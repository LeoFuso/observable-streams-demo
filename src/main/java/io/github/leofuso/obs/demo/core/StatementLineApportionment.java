package io.github.leofuso.obs.demo.core;


import java.time.*;
import java.util.*;
import java.util.function.*;

import org.springframework.context.annotation.*;

import org.apache.kafka.common.serialization.*;
import org.apache.kafka.common.utils.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.*;
import org.slf4j.*;

import io.github.leofuso.obs.demo.domain.apportionment.*;
import io.github.leofuso.obs.demo.domain.branch.*;
import io.github.leofuso.obs.demo.events.*;

import jakarta.annotation.*;

import static io.github.leofuso.obs.demo.core.configuration.TopicConfiguration.*;
import static org.apache.kafka.streams.kstream.Suppressed.*;

@Configuration
public class StatementLineApportionment {

    private static final Logger logger = LoggerFactory.getLogger(StatementLineApportionment.class);

    private final StreamsBuilder streamsBuilder;
    private final TimeWindows tumblingWindow;

    public StatementLineApportionment(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required.");

        final Duration size = Duration.ofMinutes(5);
        tumblingWindow = TimeWindows.ofSizeWithNoGrace(size);
    }

    @Bean
    public StatementLineApportionmentBranch statementLineApportionmentBranch() {
        return StatementLineBranch.produce(StatementLineApportionmentBranch.class);
    }

    @PostConstruct
    public void setup() {

        final Function<String, String> name = "stln-apportionment-%s"::formatted;
        streamsBuilder.<UUID, StatementLine>stream(
                        STATEMENT_LINE_APPORTIONMENT_RECEIPT_LINE_REPARTITION,
                        Consumed.as(name.apply("consumer")))
                .peek(
                        (key, value) -> {
                            final String message = """
                                    Processing apportionment for StatementLine [{}]""";
                            logger.debug(message, key);
                        },
                        Named.as(name.apply("peek"))
                )
                .process(
                        StatementLineApportionmentProcessorSupplier.newInstance(),
                        Named.as(name.apply("main-processor"))
                )
                .repartition(Repartitioned.as(name.apply("receipt-line")))
                .groupByKey(Grouped.as(name.apply("receipt-line-group")))
                .windowedBy(tumblingWindow)
                .aggregate(
                        ReceiptFactory::identity,
                        ReceiptFactory::aggregate,
                        Named.as(name.apply("receipt-aggregate")),
                        Materialized.<UUID, Receipt, WindowStore<Bytes, byte[]>>as(RECEIPT_STORE)
                                .withKeySerde(Serdes.UUID()) /* This is due to a bug https://issues.apache.org/jira/browse/KAFKA-9259 */
                )
                .suppress(
                        Suppressed.untilWindowCloses(
                                        BufferConfig.unbounded()
                                                .withMaxRecords(15) /* arbitrarily large value */
                                )
                                .withName(name.apply("receipt-suppressor"))
                )
                .toStream(Named.as(name.apply("receipt-stream")))
                .selectKey((key, value) -> key.key(), Named.as(name.apply("receipt-window-unwrap")))
                .to(RECEIPT, Produced.as(name.apply("receipt-producer")));
    }
}
