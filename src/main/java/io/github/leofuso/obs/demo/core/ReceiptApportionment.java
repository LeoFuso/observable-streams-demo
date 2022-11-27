package io.github.leofuso.obs.demo.core;


import io.github.leofuso.obs.demo.domain.receipt.OrderSpliterProcessorSupplier;
import io.github.leofuso.obs.demo.domain.router.ReceiptApportionmentRouter;
import io.github.leofuso.obs.demo.domain.router.StatementLineRouter;

import io.github.leofuso.obs.demo.events.StatementLine;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Configuration
public class ReceiptApportionment {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptApportionment.class);

    private static final String ROOT_NAMING = "receiptapportionment";

    private final StreamsBuilder streamsBuilder;

    public ReceiptApportionment(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required.");
    }

    @Bean
    public ReceiptApportionmentRouter receiptApportionmentRouter() {
        return StatementLineRouter.produce(ReceiptApportionmentRouter.class);
    }

    @Bean
    public void something(ReceiptApportionmentRouter router) {


        final Consumed<UUID, StatementLine> consumed = Consumed.as(ROOT_NAMING + "-consumed");
        final Named orderSplitterName = Named.as(ROOT_NAMING + "-order-splitter");
        final Grouped<UUID, StatementLine> orderGroupName = Grouped.as(ROOT_NAMING + "-grouped");

        final Duration size = Duration.ofMinutes(10);
        final Duration grace = Duration.ofSeconds(30);
        final TimeWindows tumblingWindow = TimeWindows.ofSizeAndGrace(size, grace);

        final String topic = router.topic();
        streamsBuilder.stream(topic, consumed)
                      .process(new OrderSpliterProcessorSupplier(), orderSplitterName)
                      .groupByKey(orderGroupName)
                      .windowedBy(tumblingWindow)
                .emitStrategy(EmitStrategy.onWindowUpdate());

    }

}
