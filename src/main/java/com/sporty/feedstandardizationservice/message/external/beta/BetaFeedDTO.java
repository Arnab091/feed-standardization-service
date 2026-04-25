package com.sporty.feedstandardizationservice.message.external.beta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sporty.feedstandardizationservice.message.external.FeedDTO;

/**
 * Provider Beta feed contract, discriminated by the type field.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BetaFeedOddsUpdateDTO.class, name = "ODDS"),
    @JsonSubTypes.Type(value = BetaFeedSettlementDTO.class, name = "SETTLEMENT")
})
public sealed interface BetaFeedDTO extends FeedDTO permits BetaFeedOddsUpdateDTO, BetaFeedSettlementDTO {
    @JsonProperty("event_id")
    String eventId();
}
