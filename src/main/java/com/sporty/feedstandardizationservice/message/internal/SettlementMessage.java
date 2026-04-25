package com.sporty.feedstandardizationservice.message.internal;

public record SettlementMessage(String eventId, OutCome outCome) {
    public enum OutCome {
        HOME,
        DRAW,
        AWAY
    }
}
