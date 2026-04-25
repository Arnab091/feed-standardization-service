package com.sporty.feedstandardizationservice.rest.alpha;

import com.sporty.feedstandardizationservice.message.external.alpha.AlphaFeedDTO;
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
        value = "/provider-alpha",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class ProviderAlphaFeedController extends BaseFeedController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderAlphaFeedController.class);

    public ProviderAlphaFeedController(QueueSender queueSender) {
        super(queueSender);
    }

    // Provider Alpha uses msg_type as the Jackson discriminator for request polymorphism.
    @PostMapping("/feed")
    public ResponseEntity<FeedAcceptedResponse> postFeed(@Valid @RequestBody AlphaFeedDTO feedDTO) {
        LOGGER.info(
                "Received provider-alpha feed: eventId={}, payloadType={}",
                feedDTO.eventId(),
                feedDTO.getClass().getSimpleName());
        return processFeed("provider-alpha", feedDTO);
    }
}
