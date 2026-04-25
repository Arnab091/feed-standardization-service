package com.sporty.feedstandardizationservice.message.internal;

public record OddsUpdateMessage(String eventId, float home, float draw, float away) {}
