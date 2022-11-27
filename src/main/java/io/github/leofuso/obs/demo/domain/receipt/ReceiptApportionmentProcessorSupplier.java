package io.github.leofuso.obs.demo.domain.receipt;

import java.util.UUID;

import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.api.*;

import io.github.leofuso.obs.demo.events.ReceiptApportionmentLine;
import io.github.leofuso.obs.demo.events.StatementLine;

public class ReceiptApportionmentProcessorSupplier implements ProcessorSupplier<UUID, StatementLine, UUID, ReceiptApportionmentLine> {

    private final ReceiptApportionment apportionment;

    public ReceiptApportionmentProcessorSupplier() {
        this.apportionment = new ReceiptApportionment();
    }

    @Override
    public Processor<UUID, StatementLine, UUID, ReceiptApportionmentLine> get() {
        return new ReceiptApportionmentProcessor();
    }

    private class ReceiptApportionmentProcessor extends ContextualProcessor<UUID, StatementLine, UUID, ReceiptApportionmentLine> {

        @Override
        public void process(final Record<UUID, StatementLine> record) {
            final StatementLine statementLine = record.value();
            apportionment.apportionment(statementLine)
                    .forEach(apportionment -> {

                        final UUID order = apportionment.getOrder();
                        final Record<UUID, ReceiptApportionmentLine> line =
                                record.withKey(order)
                                        .withValue(apportionment);

                        context().forward(line);
                    });
        }
    }

}
