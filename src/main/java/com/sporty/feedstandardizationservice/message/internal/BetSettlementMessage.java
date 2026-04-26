package com.sporty.feedstandardizationservice.message.internal;

public record BetSettlementMessage(String eventId, OutCome outCome) {
    public enum OutCome {
        HOME,
        DRAW,
        AWAY
    }
}
