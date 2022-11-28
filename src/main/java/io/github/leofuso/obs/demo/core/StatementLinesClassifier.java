package io.github.leofuso.obs.demo.core;

import java.util.*;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.leofuso.obs.demo.core.configuration.TopicFixture;
import io.github.leofuso.obs.demo.domain.branch.StatementLineBranch;
import io.github.leofuso.obs.demo.domain.branch.StatementLineReplicaProcessorSupplier;
import io.github.leofuso.obs.demo.events.StatementLine;

@Configuration
public class StatementLinesClassifier {

    private static final Logger logger = LoggerFactory.getLogger(StatementLinesClassifier.class);

    private static final String NAMED_SUFFIX = "stln";

    private final StreamsBuilder streamsBuilder;

    public StatementLinesClassifier(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required.");
    }

    @Bean
    public Map<String, KStream<UUID, StatementLine>> classifier(StatementLineBranch statementLineApportionmentBranch,
                                                                StatementLineBranch treasureHouseAccountingBranch) {

        final String topic = TopicFixture.APPROVED_STATEMENT_LINE;
        final Consumed<UUID, StatementLine> consumed = Consumed.as(NAMED_SUFFIX + "-consumed");
        final Named namedFilter = Named.as(NAMED_SUFFIX + "-filter");
        final Named namedBrancher = Named.as(NAMED_SUFFIX + "-brancher");
        final Named namedReplica = Named.as(NAMED_SUFFIX + "-replicated");

        final StatementLineReplicaProcessorSupplier replicateSupplier = StatementLineReplicaProcessorSupplier.replicate(
                statementLineApportionmentBranch.name(),
                treasureHouseAccountingBranch.name()
        );

        return streamsBuilder.stream(topic, consumed)
                .filter((key, value) -> Objects.nonNull(key) || Objects.nonNull(value), namedFilter)
                .peek((key, value) -> {
                    final String message = """
                            Processing StatementLine [ {} ].
                            """;
                    logger.info(message, key);
                })
                .processValues(replicateSupplier, namedReplica)
                .split(namedBrancher)
                .branch(treasureHouseAccountingBranch.supports(), treasureHouseAccountingBranch.branched())
                .branch(statementLineApportionmentBranch.supports(), statementLineApportionmentBranch.branched())
                .defaultBranch(treasureHouseAccountingBranch.branched());
    }
}
