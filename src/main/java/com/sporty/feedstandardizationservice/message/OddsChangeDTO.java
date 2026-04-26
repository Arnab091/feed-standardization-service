package com.sporty.feedstandardizationservice.message;

import com.sporty.feedstandardizationservice.message.internal.OddsChangeMessage;

/**
 * Marks an external payload that can be standardized into an internal odds update message.
 */
public interface OddsChangeDTO {
    OddsChangeMessage toOddsChangeMessage();
}
