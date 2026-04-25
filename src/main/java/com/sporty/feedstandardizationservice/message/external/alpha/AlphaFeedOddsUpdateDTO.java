package com.sporty.feedstandardizationservice.message.external.alpha;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sporty.feedstandardizationservice.message.OddsUpdateDTO;
import com.sporty.feedstandardizationservice.message.internal.OddsUpdateMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record AlphaFeedOddsUpdateDTO(
        @JsonProperty("event_id") @NotBlank(message = "event_id is required") String eventId,

        @JsonProperty("values") @Valid @NotNull(message = "values is required") AlphaFeedOdds odds)
        implements AlphaFeedDTO, OddsUpdateDTO {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaFeedOddsUpdateDTO.class);

    @Override
    public OddsUpdateMessage toOddsChangeMessage() {
        // Normalize Provider Alpha's outcome-keyed odds payload into the internal message shape.
        LOGGER.debug("Converting provider-alpha odds update for eventId={} to internal message", eventId);
        return new OddsUpdateMessage(eventId, odds.home(), odds.draw(), odds.away());
    }
}

record AlphaFeedOdds(
        @JsonProperty("1") @NotNull(message = "values.1 is required") Float home,

        @JsonProperty("X") @NotNull(message = "values.X is required") Float draw,

        @JsonProperty("2") @NotNull(message = "values.2 is required") Float away) {}
