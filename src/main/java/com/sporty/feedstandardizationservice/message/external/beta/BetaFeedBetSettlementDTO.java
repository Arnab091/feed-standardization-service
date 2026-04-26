package com.sporty.feedstandardizationservice.message.external.beta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sporty.feedstandardizationservice.message.BetSettlementDTO;
import com.sporty.feedstandardizationservice.message.internal.BetSettlementMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record BetaFeedBetSettlementDTO(
        @JsonProperty("event_id") @NotBlank(message = "event_id is required") String eventId,

        @JsonProperty("result") @NotNull(message = "result is required") BetaOutcome outcome)
        implements BetaFeedDTO, BetSettlementDTO {
    private static final Logger LOGGER = LoggerFactory.getLogger(BetaFeedBetSettlementDTO.class);

    @Override
    public BetSettlementMessage toBetSettlementMessage() {
        // Convert Provider Beta's textual outcome into the internal settlement contract.
        LOGGER.debug("Converting provider-beta settlement for eventId={} to internal message", eventId);
        return new BetSettlementMessage(eventId, outcome.toMessageOutcome());
    }
}
