package io.github.leofuso.obs.demo.core;

import java.util.*;

import org.springframework.context.annotation.*;

import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.*;

import io.github.leofuso.obs.demo.core.configuration.*;
import io.github.leofuso.obs.demo.domain.branch.*;
import io.github.leofuso.obs.demo.events.*;

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

        final String topic = TopicConfiguration.APPROVED_STATEMENT_LINE;
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
                .branch(statementLineApportionmentBranch.supports(), statementLineApportionmentBranch.branched())
                .defaultBranch(treasureHouseAccountingBranch.branched());
    }
}
