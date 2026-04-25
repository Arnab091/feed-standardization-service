package com.sporty.feedstandardizationservice.message;

import com.sporty.feedstandardizationservice.message.internal.OddsUpdateMessage;

/**
 * Marks an external payload that can be standardized into an internal odds update message.
 */
public interface OddsUpdateDTO {
    OddsUpdateMessage toOddsChangeMessage();
}
