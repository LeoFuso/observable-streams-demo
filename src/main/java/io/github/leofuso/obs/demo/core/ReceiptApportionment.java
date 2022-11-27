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

import io.github.leofuso.obs.demo.domain.receipt.ReceiptApportionmentProcessorSupplier;
import io.github.leofuso.obs.demo.domain.router.ReceiptApportionmentRouter;
import io.github.leofuso.obs.demo.domain.router.StatementLineRouter;
import io.github.leofuso.obs.demo.events.ReceiptApportionmentLine;
import io.github.leofuso.obs.demo.events.StatementLine;

@Configuration
public class ReceiptApportionment {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptApportionment.class);

    private static final String ROOT_NAMING = "receipt";

    private final StreamsBuilder streamsBuilder;

    public ReceiptApportionment(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required.");
    }

    @Bean
    public ReceiptApportionmentRouter receiptApportionmentRouter() {
        return StatementLineRouter.produce(ReceiptApportionmentRouter.class);
    }

    @Bean
    public KTable<Windowed<UUID>, ReceiptApportionmentLine> something(ReceiptApportionmentRouter router) {

        final Consumed<UUID, StatementLine> consumed = Consumed.as(ROOT_NAMING + "-consumed");
        final Named statementLineApportionmentName = Named.as(ROOT_NAMING + "-statementline-apportionment");
        final Grouped<UUID, ReceiptApportionmentLine> apportionmentGroupName = Grouped.as(ROOT_NAMING + "-grouped");
        final Named receiptApportionmentReduceName = Named.as(ROOT_NAMING + "-reduce");
        final Materialized<UUID, ReceiptApportionmentLine, WindowStore<Bytes, byte[]>> receiptApportionmentStore =
                Materialized.as(ROOT_NAMING + "-apportionment");

        final Duration size = Duration.ofMinutes(10);
        final Duration grace = Duration.ofSeconds(30);
        final TimeWindows tumblingWindow = TimeWindows.ofSizeAndGrace(size, grace);

        final String topic = router.topic();
        return streamsBuilder.stream(topic, consumed)
                .process(new ReceiptApportionmentProcessorSupplier(), statementLineApportionmentName)
                .groupByKey(apportionmentGroupName)
                .windowedBy(tumblingWindow)
                .emitStrategy(EmitStrategy.onWindowUpdate())
                .reduce((ap1, ap2) -> ap1, receiptApportionmentReduceName, receiptApportionmentStore);

    }

}
