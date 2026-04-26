package com.sporty.feedstandardizationservice.message;

import com.sporty.feedstandardizationservice.message.internal.BetSettlementMessage;

/**
 * Marks an external payload that can be standardized into an internal settlement message.
 */
public interface BetSettlementDTO {
    BetSettlementMessage toBetSettlementMessage();
}
