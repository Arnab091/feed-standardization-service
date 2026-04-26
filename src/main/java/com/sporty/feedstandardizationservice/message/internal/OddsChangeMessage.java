package com.sporty.feedstandardizationservice.message.internal;

public record OddsChangeMessage(String eventId, float home, float draw, float away) {}
