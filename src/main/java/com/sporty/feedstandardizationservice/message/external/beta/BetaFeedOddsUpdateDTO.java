package com.sporty.feedstandardizationservice.message.external.beta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sporty.feedstandardizationservice.message.OddsUpdateDTO;
import com.sporty.feedstandardizationservice.message.internal.OddsUpdateMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record BetaFeedOddsUpdateDTO(
        @JsonProperty("event_id") @NotBlank(message = "event_id is required") String eventId,

        @Valid @NotNull(message = "odds is required") BetaFeedOdds odds)
        implements BetaFeedDTO, OddsUpdateDTO {
    private static final Logger LOGGER = LoggerFactory.getLogger(BetaFeedOddsUpdateDTO.class);

    @Override
    public OddsUpdateMessage toOddsChangeMessage() {
        // Provider Beta already sends named odds fields, so normalization is mostly shape alignment.
        LOGGER.debug("Converting provider-beta odds update for eventId={} to internal message", eventId);
        return new OddsUpdateMessage(eventId, odds.home(), odds.draw(), odds.away());
    }
}

record BetaFeedOdds(
        @NotNull(message = "odds.home is required") Float home,
        @NotNull(message = "odds.draw is required") Float draw,
        @NotNull(message = "odds.away is required") Float away) {}
