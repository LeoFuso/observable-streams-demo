package io.github.leofuso.obs.demo.domain.apportionment;

import java.util.UUID;

import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.api.*;

import io.github.leofuso.obs.demo.events.ReceiptLine;
import io.github.leofuso.obs.demo.events.StatementLine;

public class StatementLineApportionmentProcessorSupplier implements ProcessorSupplier<UUID, StatementLine, UUID, ReceiptLine> {

    private final ReceiptFactory factory;

    public StatementLineApportionmentProcessorSupplier() {
        this.factory = ReceiptFactory.getInstance();
    }

    @Override
    public Processor<UUID, StatementLine, UUID, ReceiptLine> get() {
        return new StatementLineApportionmentProcessor();
    }

    private class StatementLineApportionmentProcessor extends ContextualProcessor<UUID, StatementLine, UUID, ReceiptLine> {

        @Override
        public void process(final Record<UUID, StatementLine> record) {
            final StatementLine statementLine = record.value();
            factory.performApportion(statementLine)
                    .forEach((order, receiptLine) -> {

                        final Record<UUID, ReceiptLine> line =
                                record.withKey(order)
                                        .withValue(receiptLine);

                        context().forward(line);
                    });
        }
    }
}
