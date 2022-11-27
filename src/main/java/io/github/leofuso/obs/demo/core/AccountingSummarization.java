package io.github.leofuso.obs.demo.core;

import java.util.Objects;

import org.apache.kafka.streams.StreamsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.leofuso.obs.demo.domain.router.*;

@Configuration
public class AccountingSummarization {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptApportionment.class);

    private static final String ROOT_NAMING = "accountingsummarization";

    private final StreamsBuilder streamsBuilder;

    public AccountingSummarization(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required.");
    }

    @Bean
    public AccoutingSummarizationRouter accountingSummarizationRouter() {
        return StatementLineRouter.produce(AccoutingSummarizationRouter.class);
    }

    @Bean
    public UnknownSegmentRouter unknownSegmentRouter() {
        return StatementLineRouter.produce(UnknownSegmentRouter.class);
    }

}
