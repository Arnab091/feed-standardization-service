package com.sporty.feedstandardizationservice.queue;

import com.sporty.feedstandardizationservice.message.internal.OddsUpdateMessage;
import com.sporty.feedstandardizationservice.message.internal.SettlementMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockQueueSender implements QueueSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockQueueSender.class);

    @Override
    public void publishBetSettlementMessage(SettlementMessage settlementMessage) {
        // This mock implementation logs the outbound message instead of sending it to a real broker.
        LOGGER.info("Publishing settlement message to mock queue: {}", settlementMessage);
    }

    @Override
    public void publishOddsChangeMessage(OddsUpdateMessage oddsUpdateMessage) {
        // This mock implementation logs the outbound message instead of sending it to a real broker.
        LOGGER.info("Publishing odds update message to mock queue: {}", oddsUpdateMessage);
    }
}
