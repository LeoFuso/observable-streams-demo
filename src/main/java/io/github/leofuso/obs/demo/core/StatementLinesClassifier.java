package io.github.leofuso.obs.demo.core;

import java.util.*;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.leofuso.obs.demo.core.configuration.TopicFixture;
import io.github.leofuso.obs.demo.domain.router.StatementLineRouter;
import io.github.leofuso.obs.demo.events.StatementLine;

@Configuration
public class StatementLinesClassifier {

    private static final Logger logger = LoggerFactory.getLogger(StatementLinesClassifier.class);

    private static final String ROOT_NAMING = "statementline";

    private final StreamsBuilder streamsBuilder;

    public StatementLinesClassifier(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required.");
    }

    @Bean
    public Map<String, KStream<UUID, StatementLine>> classifier(StatementLineRouter receiptApportionmentRouter,
                                                                StatementLineRouter accountingSummarizationRouter,
                                                                StatementLineRouter unknownSegmentRouter) {

        final String source = TopicFixture.APPROVED_STATEMENT_LINE;
        final Consumed<UUID, StatementLine> consumer = Consumed.as(ROOT_NAMING + "-consumer");
        final Named nullFilter = Named.as(ROOT_NAMING + "-null-filter");
        final Named splitter = Named.as(ROOT_NAMING + "-splitter");
        return streamsBuilder.stream(source, consumer)
                             .filter((key, value) -> Objects.nonNull(key) || Objects.nonNull(value), nullFilter)
                             .split(splitter)
                             .branch(unknownSegmentRouter.supports(), unknownSegmentRouter.branched())
                             .branch(accountingSummarizationRouter.supports(), accountingSummarizationRouter.branched())
                             .branch(receiptApportionmentRouter.supports(), receiptApportionmentRouter.branched())
                             .defaultBranch(unknownSegmentRouter.branched());
    }
}
