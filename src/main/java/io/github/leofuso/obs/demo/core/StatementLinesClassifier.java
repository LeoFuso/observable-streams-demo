package io.github.leofuso.obs.demo.core;

import io.github.leofuso.obs.demo.core.configuration.TopicConfiguration;
import io.github.leofuso.obs.demo.domain.branch.StatementLineBranch;
import io.github.leofuso.obs.demo.domain.branch.StatementLineReplicaProcessorSupplier;
import io.github.leofuso.obs.demo.events.StatementLine;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
        final Named namedBranched = Named.as(NAMED_SUFFIX + "-branched");
        final Named namedReplica = Named.as(NAMED_SUFFIX + "-replicated");

        final StatementLineReplicaProcessorSupplier replicateSupplier = StatementLineReplicaProcessorSupplier.replicate(
                statementLineApportionmentBranch.name(),
                treasureHouseAccountingBranch.name()
        );

        return streamsBuilder.stream(topic, consumed)
                .filter((key, value) -> Objects.nonNull(key) || Objects.nonNull(value), namedFilter)
                .peek((key, value) -> {
                    final String message = """
                            Processing StatementLine [{}]""";
                    logger.info(message, key);
                })
                .processValues(replicateSupplier, namedReplica)
                .split(namedBranched)
                .branch(statementLineApportionmentBranch.supports(), statementLineApportionmentBranch.branched())
                .defaultBranch(treasureHouseAccountingBranch.branched());
    }
}
