package com.sporty.feedstandardizationservice.queue;

import com.sporty.feedstandardizationservice.message.internal.BetSettlementMessage;
import com.sporty.feedstandardizationservice.message.internal.OddsChangeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockQueueSender implements QueueSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockQueueSender.class);

    @Override
    public void publishBetSettlementMessage(BetSettlementMessage betSettlementMessage) {
        // This mock implementation logs the outbound message instead of sending it to a real broker.
        LOGGER.info("Publishing BET_SETTLEMENT message to mock queue: {}", betSettlementMessage);
    }

    @Override
    public void publishOddsChangeMessage(OddsChangeMessage oddsChangeMessage) {
        // This mock implementation logs the outbound message instead of sending it to a real broker.
        LOGGER.info("Publishing ODDS_CHANGE message to mock queue: {}", oddsChangeMessage);
    }
}
