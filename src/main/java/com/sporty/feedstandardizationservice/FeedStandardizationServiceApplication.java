package com.sporty.feedstandardizationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FeedStandardizationServiceApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedStandardizationServiceApplication.class);

    public static void main(String[] args) {
        // Log startup explicitly so the service bootstrap is easy to spot in aggregated logs.
        LOGGER.info("Starting feed standardization service");
        SpringApplication.run(FeedStandardizationServiceApplication.class, args);
    }
}
