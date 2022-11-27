package io.github.leofuso.obs.demo.domain.receipt;

import io.github.leofuso.obs.demo.events.StatementLine;

import org.apache.kafka.streams.processor.api.*;
import org.apache.kafka.streams.processor.api.Record;

import java.util.UUID;

public class OrderSpliterProcessorSupplier implements ProcessorSupplier<UUID, StatementLine, UUID, StatementLine> {

    private final StatementLineSpliter spliter;

    public OrderSpliterProcessorSupplier() {
        this.spliter = new StatementLineSpliter();
    }

    @Override
    public Processor<UUID, StatementLine, UUID, StatementLine> get() {
        return new OrderSpliterProcessor();
    }

    private class OrderSpliterProcessor extends ContextualProcessor<UUID, StatementLine, UUID, StatementLine> {

        @Override
        public void process(final Record<UUID, StatementLine> record) {
            spliter.byOrder(record)
                   .forEach(splitted -> context().forward(splitted));
        }
    }

}
