package com.sporty.feedstandardizationservice.rest;

import com.sporty.feedstandardizationservice.message.OddsUpdateDTO;
import com.sporty.feedstandardizationservice.message.SettlementDTO;
import com.sporty.feedstandardizationservice.message.external.FeedDTO;
import com.sporty.feedstandardizationservice.queue.QueueSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public abstract class BaseFeedController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFeedController.class);
    private final QueueSender queueSender;

    public BaseFeedController(QueueSender queueSender) {
        this.queueSender = queueSender;
    }

    // Centralize the provider-agnostic routing so both controllers stay thin.
    protected ResponseEntity<FeedAcceptedResponse> processFeed(String provider, FeedDTO feedDTO) {
        switch (feedDTO) {
            case OddsUpdateDTO oddsUpdateDTO -> {
                LOGGER.debug(
                        "Processing odds update for eventId={} with payloadType={}",
                        feedDTO.eventId(),
                        feedDTO.getClass().getSimpleName());
                queueSender.publishOddsChangeMessage(oddsUpdateDTO.toOddsChangeMessage());
                return acceptedResponse(provider, feedDTO, "odds_update");
            }
            case SettlementDTO settlementDTO -> {
                LOGGER.debug(
                        "Processing settlement for eventId={} with payloadType={}",
                        feedDTO.eventId(),
                        feedDTO.getClass().getSimpleName());
                queueSender.publishBetSettlementMessage(settlementDTO.toBetSettlementMessage());
                return acceptedResponse(provider, feedDTO, "settlement");
            }
            default -> {
                LOGGER.warn(
                        "Ignoring unsupported feed payloadType={} for eventId={}",
                        feedDTO.getClass().getName(),
                        feedDTO.eventId());
                throw new IllegalArgumentException(
                        "Unsupported feed payload type: " + feedDTO.getClass().getName());
            }
        }
    }

    private ResponseEntity<FeedAcceptedResponse> acceptedResponse(String provider, FeedDTO feedDTO, String feedType) {
        FeedAcceptedResponse response = FeedAcceptedResponse.accepted(provider, feedDTO.eventId(), feedType);
        LOGGER.info("Accepted {} feed for provider={} and eventId={}", feedType, provider, feedDTO.eventId());
        return ResponseEntity.accepted().body(response);
    }
}
