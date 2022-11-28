package io.github.leofuso.obs.demo.core;


import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.leofuso.obs.demo.domain.apportionment.ReceiptFactory;
import io.github.leofuso.obs.demo.domain.apportionment.StatementLineApportionmentProcessorSupplier;
import io.github.leofuso.obs.demo.domain.branch.StatementLineApportionmentBranch;
import io.github.leofuso.obs.demo.domain.branch.StatementLineBranch;
import io.github.leofuso.obs.demo.events.*;

@Configuration
public class StatementLineApportionment {

    private static final Logger logger = LoggerFactory.getLogger(StatementLineApportionment.class);

    private static final String NAMED_SUFFIX = "stln-apportionment";

    private final StreamsBuilder streamsBuilder;

    public StatementLineApportionment(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required.");
    }

    @Bean
    public StatementLineApportionmentBranch statementLineApportionmentBranch() {
        return StatementLineBranch.produce(StatementLineApportionmentBranch.class);
    }

    @Bean
    public KTable<Windowed<UUID>, Receipt> receiptStore(StatementLineApportionmentBranch branch) {

        final Consumed<UUID, StatementLine> consumed = Consumed.as(NAMED_SUFFIX + "-consumed");
        final Named namedApportionmentProcessor = Named.as(NAMED_SUFFIX + "-processed");
        final Grouped<UUID, ReceiptLine> namdGroup = Grouped.as(NAMED_SUFFIX + "-grouped");
        final Named namedReceiptAggregate = Named.as(NAMED_SUFFIX + "-aggregated");
        final Materialized<UUID, Receipt, WindowStore<Bytes, byte[]>> receiptStore = Materialized.as("receipt-store");

        final Duration size = Duration.ofMinutes(10);
        final Duration grace = Duration.ofMinutes(1);
        final TimeWindows window = TimeWindows.ofSizeAndGrace(size, grace);

        final String topic = branch.topic();
        return streamsBuilder.stream(topic, consumed)
                .peek((key, value) -> {
                    final String message = """
                            Processing apportionment for StatementLine [ {} ].
                            """;
                    logger.info(message, key);
                })
                .process(new StatementLineApportionmentProcessorSupplier(), namedApportionmentProcessor)
                .groupByKey(namdGroup)
                .windowedBy(window)
                .emitStrategy(EmitStrategy.onWindowUpdate())
                .aggregate(
                        ReceiptFactory::identity,
                        ReceiptFactory::aggregate,
                        namedReceiptAggregate,
                        receiptStore
                );
    }

}