package com.sporty.feedstandardizationservice.rest;

/**
 * Success response returned when a provider feed is accepted for downstream processing.
 */
public record FeedAcceptedResponse(String status, String provider, String eventId, String feedType) {
    public static FeedAcceptedResponse accepted(String provider, String eventId, String feedType) {
        return new FeedAcceptedResponse("accepted", provider, eventId, feedType);
    }
}
