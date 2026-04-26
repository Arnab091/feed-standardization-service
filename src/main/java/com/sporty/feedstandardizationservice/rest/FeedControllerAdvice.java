package com.sporty.feedstandardizationservice.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.RecordComponent;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.InvalidTypeIdException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.ValueInstantiationException;

@RestControllerAdvice
public class FeedControllerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedControllerAdvice.class);
    private static final Pattern INVALID_VALUE_PATTERN = Pattern.compile("^[^:]+:\\s*(.+?)\\.\\s.*$");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        LOGGER.warn("Request body validation failed", exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT, "Request validation failed. See violations for details.");
        problemDetail.setTitle("Request validation failed");
        problemDetail.setProperty("errorCategory", "VALIDATION_FAILED");
        problemDetail.setProperty(
                "violations",
                exception.getBindingResult().getFieldErrors().stream()
                        .map(fieldError ->
                                toViolation(exception.getBindingResult().getTarget(), fieldError))
                        .toList());
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        InvalidTypeIdException invalidTypeIdException = findCause(exception, InvalidTypeIdException.class);
        if (invalidTypeIdException != null) {
            return unsupportedJsonSubtype(invalidTypeIdException);
        }

        InvalidFormatException invalidFormatException = findCause(exception, InvalidFormatException.class);
        if (invalidFormatException != null) {
            return invalidJsonValue(invalidFormatException);
        }

        ValueInstantiationException valueInstantiationException =
                findCause(exception, ValueInstantiationException.class);
        if (valueInstantiationException != null) {
            return invalidInstantiatedValue(valueInstantiationException);
        }

        MismatchedInputException mismatchedInputException = findCause(exception, MismatchedInputException.class);
        if (mismatchedInputException != null) {
            return invalidJsonStructure(mismatchedInputException);
        }

        StreamReadException streamReadException = findCause(exception, StreamReadException.class);
        if (streamReadException != null) {
            return malformedJson(streamReadException);
        }

        LOGGER.warn("Failed to read request body", exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body could not be read. Ensure the payload is valid JSON for the selected feed type.");
        problemDetail.setTitle("Unreadable request body");
        problemDetail.setProperty("errorCategory", "UNREADABLE_JSON");
        return problemDetail;
    }

    private ProblemDetail malformedJson(StreamReadException exception) {
        TokenStreamLocation location = exception.getLocation();
        String detail = "Request body contains malformed JSON";
        if (location != null) {
            detail += " at line %d, column %d".formatted(location.getLineNr(), location.getColumnNr());
        }
        detail += ".";

        LOGGER.warn("Malformed JSON payload: {}", detail, exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Malformed JSON request");
        problemDetail.setProperty("errorCategory", "MALFORMED_JSON");
        if (location != null) {
            problemDetail.setProperty("line", location.getLineNr());
            problemDetail.setProperty("column", location.getColumnNr());
        }
        return problemDetail;
    }

    private ProblemDetail unsupportedJsonSubtype(InvalidTypeIdException exception) {
        String field = formatPath(exception);
        String detail = field.isBlank()
                ? "JSON type discriminator has unsupported value '%s'.".formatted(exception.getTypeId())
                : "JSON field '%s' has unsupported value '%s'.".formatted(field, exception.getTypeId());

        LOGGER.warn("Unsupported JSON subtype for request body: {}", detail, exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, detail);
        problemDetail.setTitle("Unsupported feed type");
        problemDetail.setProperty("errorCategory", "INVALID_TYPE_ID");
        if (!field.isBlank()) {
            problemDetail.setProperty("field", field);
        }
        problemDetail.setProperty("rejectedValue", exception.getTypeId());
        return problemDetail;
    }

    private ProblemDetail invalidJsonValue(InvalidFormatException exception) {
        String path = formatPath(exception);
        String detail = "JSON field '%s' has value '%s' that cannot be converted to %s."
                .formatted(path, exception.getValue(), exception.getTargetType().getSimpleName());

        LOGGER.warn("Invalid JSON value: {}", detail, exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, detail);
        problemDetail.setTitle("Invalid JSON value");
        problemDetail.setProperty("errorCategory", "INVALID_FORMAT");
        problemDetail.setProperty("field", path);
        problemDetail.setProperty("rejectedValue", exception.getValue());
        problemDetail.setProperty("expectedType", exception.getTargetType().getSimpleName());
        return problemDetail;
    }

    private ProblemDetail invalidInstantiatedValue(ValueInstantiationException exception) {
        String path = formatPath(exception);
        String expectedType = exception.getType().getRawClass().getSimpleName();
        String rejectedValue = extractRejectedValue(exception);
        String detail = rejectedValue == null
                ? "JSON field '%s' could not be converted to %s.".formatted(path, expectedType)
                : "JSON field '%s' has value '%s' that cannot be converted to %s."
                        .formatted(path, rejectedValue, expectedType);

        LOGGER.warn("Invalid instantiated JSON value: {}", detail, exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, detail);
        problemDetail.setTitle("Invalid JSON value");
        problemDetail.setProperty("errorCategory", "INVALID_FORMAT");
        if (!path.isBlank()) {
            problemDetail.setProperty("field", path);
        }
        if (rejectedValue != null) {
            problemDetail.setProperty("rejectedValue", rejectedValue);
        }
        problemDetail.setProperty("expectedType", expectedType);
        return problemDetail;
    }

    private ProblemDetail invalidJsonStructure(MismatchedInputException exception) {
        String path = formatPath(exception);
        String detail = path.isBlank()
                ? "JSON body structure is invalid for this endpoint."
                : "JSON field '%s' has an invalid structure for this endpoint.".formatted(path);

        LOGGER.warn("Invalid JSON structure: {}", detail, exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, detail);
        problemDetail.setTitle("Invalid JSON structure");
        problemDetail.setProperty("errorCategory", "MISMATCHED_INPUT");
        if (!path.isBlank()) {
            problemDetail.setProperty("field", path);
        }
        return problemDetail;
    }

    private String formatPath(JacksonException exception) {
        return exception.getPath().stream().map(this::formatReference).collect(Collectors.joining("."));
    }

    private String extractRejectedValue(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                Matcher matcher = INVALID_VALUE_PATTERN.matcher(message);
                if (matcher.matches()) {
                    return matcher.group(1);
                }
            }
            current = current.getCause();
        }
        return null;
    }

    private <T extends Throwable> T findCause(Throwable exception, Class<T> type) {
        Throwable current = exception;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private String formatReference(JacksonException.Reference reference) {
        if (reference.getPropertyName() != null) {
            return reference.getPropertyName();
        }
        return "[%d]".formatted(reference.getIndex());
    }

    private Map<String, Object> toViolation(Object target, FieldError fieldError) {
        String message = fieldError.getDefaultMessage() == null ? "Validation failed" : fieldError.getDefaultMessage();
        return Map.of(
                "field",
                resolveViolationField(target, fieldError, message),
                "message",
                message,
                "rejectedValue",
                fieldError.getRejectedValue() == null ? "null" : fieldError.getRejectedValue());
    }

    private String resolveViolationField(Object target, FieldError fieldError, String message) {
        if (message.endsWith(" is required")) {
            return message.substring(0, message.length() - " is required".length());
        }
        return resolveJsonFieldPath(target, fieldError.getField());
    }

    private String resolveJsonFieldPath(Object target, String fieldPath) {
        if (target == null || fieldPath == null || fieldPath.isBlank()) {
            return fieldPath;
        }

        StringBuilder jsonPath = new StringBuilder();
        Class<?> currentType = target.getClass();
        for (String segment : fieldPath.split("\\.")) {
            if (!jsonPath.isEmpty()) {
                jsonPath.append('.');
            }

            RecordComponent component = findRecordComponent(currentType, segment);
            if (component != null) {
                JsonProperty jsonProperty = component.getAnnotation(JsonProperty.class);
                jsonPath.append(
                        jsonProperty != null && !jsonProperty.value().isBlank() ? jsonProperty.value() : segment);
                currentType = component.getType();
                continue;
            }

            jsonPath.append(segment);
        }
        return jsonPath.toString();
    }

    private RecordComponent findRecordComponent(Class<?> type, String name) {
        if (!type.isRecord()) {
            return null;
        }
        for (RecordComponent recordComponent : type.getRecordComponents()) {
            if (recordComponent.getName().equals(name)) {
                return recordComponent;
            }
        }
        return null;
    }
}
