package com.sporty.feedstandardizationservice.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sporty.feedstandardizationservice.message.internal.OddsUpdateMessage;
import com.sporty.feedstandardizationservice.message.internal.SettlementMessage;
import com.sporty.feedstandardizationservice.queue.QueueSender;
import com.sporty.feedstandardizationservice.rest.alpha.ProviderAlphaFeedController;
import com.sporty.feedstandardizationservice.rest.beta.ProviderBetaFeedController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class ProviderFeedControllerTest {

    private MockMvc mockMvc;
    private CapturingQueueSender queueSender;

    @BeforeEach
    void setUp() {
        queueSender = new CapturingQueueSender();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ProviderAlphaFeedController(queueSender), new ProviderBetaFeedController(queueSender))
                .setValidator(validator)
                .setControllerAdvice(new FeedControllerAdvice())
                .build();
    }

    @Test
    void alphaOddsFeedIsDeserializedAndPublished() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": "match-1",
                                  "values": {
                                    "1": 1.1,
                                    "X": 2.2,
                                    "2": 3.3
                                  }
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "status": "accepted",
                          "provider": "provider-alpha",
                          "eventId": "match-1",
                          "feedType": "odds_update"
                        }
                        """, false));

        assertEquals(new OddsUpdateMessage("match-1", 1.1f, 2.2f, 3.3f), queueSender.lastOddsUpdateMessage);
        assertNull(queueSender.lastSettlementMessage);
    }

    @Test
    void alphaSettlementFeedIsDeserializedAndPublished() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "settlement",
                                  "event_id": "match-3",
                                  "outcome": "X"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "status": "accepted",
                          "provider": "provider-alpha",
                          "eventId": "match-3",
                          "feedType": "settlement"
                        }
                        """, false));

        assertEquals(
                new SettlementMessage("match-3", SettlementMessage.OutCome.DRAW), queueSender.lastSettlementMessage);
        assertNull(queueSender.lastOddsUpdateMessage);
    }

    @Test
    void betaOddsFeedIsDeserializedAndPublished() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": "match-4",
                                  "odds": {
                                    "home": 1.9,
                                    "draw": 2.8,
                                    "away": 4.7
                                  }
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "status": "accepted",
                          "provider": "provider-beta",
                          "eventId": "match-4",
                          "feedType": "odds_update"
                        }
                        """, false));

        assertEquals(new OddsUpdateMessage("match-4", 1.9f, 2.8f, 4.7f), queueSender.lastOddsUpdateMessage);
        assertNull(queueSender.lastSettlementMessage);
    }

    @Test
    void betaSettlementFeedIsDeserializedAndPublished() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "SETTLEMENT",
                                  "event_id": "match-2",
                                  "result": "away"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "status": "accepted",
                          "provider": "provider-beta",
                          "eventId": "match-2",
                          "feedType": "settlement"
                        }
                        """, false));

        assertEquals(
                new SettlementMessage("match-2", SettlementMessage.OutCome.AWAY), queueSender.lastSettlementMessage);
        assertNull(queueSender.lastOddsUpdateMessage);
    }

    @Test
    void malformedJsonReturnsBadRequestProblemDetail() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": "broken-json",
                                  "values": {
                                    "1": 1.1,
                                    "X": 2.2,
                                    "2": 3.3
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(content().json("""
                        {
                          "status": 400,
                          "title": "Malformed JSON request",
                          "errorCategory": "MALFORMED_JSON"
                        }
                        """, false));

        assertNull(queueSender.lastOddsUpdateMessage);
        assertNull(queueSender.lastSettlementMessage);
    }

    @Test
    void unsupportedSubtypeReturnsUnprocessableContentProblemDetail() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "UNKNOWN",
                                  "event_id": "match-6",
                                  "result": "away"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(content().json("""
                        {
                          "status": 422,
                          "title": "Unsupported feed type",
                          "errorCategory": "INVALID_TYPE_ID",
                          "rejectedValue": "UNKNOWN"
                        }
                        """, false));

        assertNull(queueSender.lastOddsUpdateMessage);
        assertNull(queueSender.lastSettlementMessage);
    }

    @Test
    void invalidEnumValueReturnsUnprocessableContentProblemDetail() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "settlement",
                                  "event_id": "match-7",
                                  "outcome": "INVALID"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(content().json("""
                        {
                          "status": 422,
                          "title": "Invalid JSON value",
                          "errorCategory": "INVALID_FORMAT",
                          "field": "outcome",
                          "rejectedValue": "INVALID",
                          "expectedType": "AlphaOutcome"
                        }
                        """, false));

        assertNull(queueSender.lastOddsUpdateMessage);
        assertNull(queueSender.lastSettlementMessage);
    }

    @Test
    void missingAlphaEventIdReturnsValidationProblemDetail() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "settlement",
                                  "outcome": "X"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(content().json("""
                        {
                          "status": 422,
                          "title": "Request validation failed",
                          "errorCategory": "VALIDATION_FAILED",
                          "violations": [
                            {
                              "field": "event_id",
                              "message": "event_id is required",
                              "rejectedValue": "null"
                            }
                          ]
                        }
                        """, false));

        assertNull(queueSender.lastOddsUpdateMessage);
        assertNull(queueSender.lastSettlementMessage);
    }

    @Test
    void nullBetaResultReturnsValidationProblemDetail() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "SETTLEMENT",
                                  "event_id": "match-8",
                                  "result": null
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(content().json("""
                        {
                          "status": 422,
                          "title": "Request validation failed",
                          "errorCategory": "VALIDATION_FAILED",
                          "violations": [
                            {
                              "field": "result",
                              "message": "result is required",
                              "rejectedValue": "null"
                            }
                          ]
                        }
                        """, false));

        assertNull(queueSender.lastOddsUpdateMessage);
        assertNull(queueSender.lastSettlementMessage);
    }

    @Test
    void missingNestedBetaOddsValueReturnsValidationProblemDetail() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": "match-9",
                                  "odds": {
                                    "draw": 2.8,
                                    "away": 4.7
                                  }
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(content().json("""
                        {
                          "status": 422,
                          "title": "Request validation failed",
                          "errorCategory": "VALIDATION_FAILED",
                          "violations": [
                            {
                              "field": "odds.home",
                              "message": "odds.home is required",
                              "rejectedValue": "null"
                            }
                          ]
                        }
                        """, false));

        assertNull(queueSender.lastOddsUpdateMessage);
        assertNull(queueSender.lastSettlementMessage);
    }

    private static final class CapturingQueueSender implements QueueSender {
        private SettlementMessage lastSettlementMessage;
        private OddsUpdateMessage lastOddsUpdateMessage;

        @Override
        public void publishBetSettlementMessage(SettlementMessage settlementMessage) {
            this.lastSettlementMessage = settlementMessage;
        }

        @Override
        public void publishOddsChangeMessage(OddsUpdateMessage oddsUpdateMessage) {
            this.lastOddsUpdateMessage = oddsUpdateMessage;
        }
    }
}
