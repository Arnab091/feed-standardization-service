package com.sporty.feedstandardizationservice.message.external.beta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.sporty.feedstandardizationservice.message.internal.BetSettlementMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum BetaOutcome {
    HOME("home", BetSettlementMessage.OutCome.HOME),
    DRAW("draw", BetSettlementMessage.OutCome.DRAW),
    AWAY("away", BetSettlementMessage.OutCome.AWAY);

    private final String code;
    private final BetSettlementMessage.OutCome mappedOutcome;
    private static final Logger LOGGER = LoggerFactory.getLogger(BetaOutcome.class);

    BetaOutcome(String code, BetSettlementMessage.OutCome mappedOutcome) {
        this.code = code;
        this.mappedOutcome = mappedOutcome;
    }

    @JsonCreator
    public static BetaOutcome fromCode(String code) {
        // Provider Beta emits descriptive lowercase outcome codes.
        LOGGER.debug("Resolving provider-beta outcome code={}", code);
        return switch (code) {
            case "home" -> HOME;
            case "draw" -> DRAW;
            case "away" -> AWAY;
            default ->
                throw new IllegalArgumentException("Invalid outcome: " + code + ". Expected one of [home, draw, away]");
        };
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public BetSettlementMessage.OutCome toMessageOutcome() {
        LOGGER.debug("Mapped provider-beta outcome {} to internal outcome {}", code, mappedOutcome);
        return mappedOutcome;
    }
}
