package com.sporty.feedstandardizationservice.message.external.alpha;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sporty.feedstandardizationservice.message.external.FeedDTO;

/**
 * Provider Alpha feed contract, discriminated by the msg_type field.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "msg_type",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AlphaFeedOddsUpdateDTO.class, name = "odds_update"),
    @JsonSubTypes.Type(value = AlphaFeedSettlementDTO.class, name = "settlement")
})
public sealed interface AlphaFeedDTO extends FeedDTO permits AlphaFeedOddsUpdateDTO, AlphaFeedSettlementDTO {
    @JsonProperty("event_id")
    String eventId();
}
