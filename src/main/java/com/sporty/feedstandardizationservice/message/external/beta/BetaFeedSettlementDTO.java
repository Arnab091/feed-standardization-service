package com.sporty.feedstandardizationservice.message.external.beta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sporty.feedstandardizationservice.message.SettlementDTO;
import com.sporty.feedstandardizationservice.message.internal.SettlementMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record BetaFeedSettlementDTO(
        @JsonProperty("event_id") @NotBlank(message = "event_id is required") String eventId,

        @JsonProperty("result") @NotNull(message = "result is required") BetaOutcome outcome)
        implements BetaFeedDTO, SettlementDTO {
    private static final Logger LOGGER = LoggerFactory.getLogger(BetaFeedSettlementDTO.class);

    @Override
    public SettlementMessage toBetSettlementMessage() {
        // Convert Provider Beta's textual outcome into the internal settlement contract.
        LOGGER.debug("Converting provider-beta settlement for eventId={} to internal message", eventId);
        return new SettlementMessage(eventId, outcome.toMessageOutcome());
    }
}
