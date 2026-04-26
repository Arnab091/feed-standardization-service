package com.sporty.feedstandardizationservice.queue;

import com.sporty.feedstandardizationservice.message.internal.BetSettlementMessage;
import com.sporty.feedstandardizationservice.message.internal.OddsChangeMessage;

public interface QueueSender {
    void publishBetSettlementMessage(BetSettlementMessage betSettlementMessage);

    void publishOddsChangeMessage(OddsChangeMessage oddsChangeMessage);
}
