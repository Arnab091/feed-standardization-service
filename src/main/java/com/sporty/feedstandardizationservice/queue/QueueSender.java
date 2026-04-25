package com.sporty.feedstandardizationservice.queue;

import com.sporty.feedstandardizationservice.message.internal.OddsUpdateMessage;
import com.sporty.feedstandardizationservice.message.internal.SettlementMessage;

public interface QueueSender {
    void publishBetSettlementMessage(SettlementMessage settlementMessage);

    void publishOddsChangeMessage(OddsUpdateMessage oddsUpdateMessage);
}
