package com.sporty.feedstandardizationservice.message.external.alpha;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sporty.feedstandardizationservice.message.BetSettlementDTO;
import com.sporty.feedstandardizationservice.message.internal.BetSettlementMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record AlphaFeedBetSettlementDTO(
        @JsonProperty("event_id") @NotBlank(message = "event_id is required") String eventId,

        @JsonProperty("outcome") @NotNull(message = "outcome is required") AlphaOutcome outcome)
        implements AlphaFeedDTO, BetSettlementDTO {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaFeedBetSettlementDTO.class);

    @Override
    public BetSettlementMessage toBetSettlementMessage() {
        // Convert Provider Alpha's symbolic outcome into the internal settlement contract.
        LOGGER.debug("Converting provider-alpha settlement for eventId={} to internal message", eventId);
        return new BetSettlementMessage(eventId, outcome.toMessageOutcome());
    }
}
