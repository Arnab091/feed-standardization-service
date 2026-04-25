package com.sporty.feedstandardizationservice.message.external.alpha;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.sporty.feedstandardizationservice.message.internal.SettlementMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AlphaOutcome {
    HOME("1", SettlementMessage.OutCome.HOME),
    DRAW("X", SettlementMessage.OutCome.DRAW),
    AWAY("2", SettlementMessage.OutCome.AWAY);

    private final String code;
    private final SettlementMessage.OutCome mappedOutcome;
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaOutcome.class);

    AlphaOutcome(String code, SettlementMessage.OutCome mappedOutcome) {
        this.code = code;
        this.mappedOutcome = mappedOutcome;
    }

    @JsonCreator
    public static AlphaOutcome fromCode(String code) {
        // Provider Alpha emits compact 1/X/2 codes that need to be expanded before dispatch.
        LOGGER.debug("Resolving provider-alpha outcome code={}", code);
        return switch (code) {
            case "1" -> HOME;
            case "X" -> DRAW;
            case "2" -> AWAY;
            default -> throw new IllegalArgumentException("Invalid outcome: " + code + ". Expected one of [1, X, 2]");
        };
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public SettlementMessage.OutCome toMessageOutcome() {
        LOGGER.debug("Mapped provider-alpha outcome {} to internal outcome {}", code, mappedOutcome);
        return mappedOutcome;
    }
}
