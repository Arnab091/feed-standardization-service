package com.sporty.feedstandardizationservice.message.external;

/**
 * Common contract shared by all provider payloads after request deserialization.
 */
public interface FeedDTO {
    String eventId();
}
