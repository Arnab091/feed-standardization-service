package com.sporty.feedstandardizationservice.rest.beta;

import com.sporty.feedstandardizationservice.message.external.beta.BetaFeedDTO;
import com.sporty.feedstandardizationservice.queue.QueueSender;
import com.sporty.feedstandardizationservice.rest.BaseFeedController;
import com.sporty.feedstandardizationservice.rest.FeedAcceptedResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "/provider-beta",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class ProviderBetaFeedController extends BaseFeedController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBetaFeedController.class);

    public ProviderBetaFeedController(QueueSender queueSender) {
        super(queueSender);
    }

    // Provider Beta uses type as the Jackson discriminator for request polymorphism.
    @PostMapping("/feed")
    public ResponseEntity<FeedAcceptedResponse> postFeed(@Valid @RequestBody BetaFeedDTO feedDTO) {
        LOGGER.info(
                "Received provider-beta feed: eventId={}, payloadType={}",
                feedDTO.eventId(),
                feedDTO.getClass().getSimpleName());
        return processFeed("provider-beta", feedDTO);
    }
}
