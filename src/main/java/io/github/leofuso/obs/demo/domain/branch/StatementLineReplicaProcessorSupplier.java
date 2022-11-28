package io.github.leofuso.obs.demo.domain.branch;

import java.util.*;

import org.apache.kafka.streams.processor.api.*;

import io.github.leofuso.obs.demo.events.StatementLine;

public class StatementLineReplicaProcessorSupplier implements FixedKeyProcessorSupplier<UUID, StatementLine, StatementLine> {

    public static final String REPLICA_ID_KEY = "REPLICA_ID";
    public static final String REPLICA_OPERATION_KEY = "REPLICA_OP";

    private final Iterable<String> identifiers;

    private StatementLineReplicaProcessorSupplier(final Iterable<String> identifiers) {
        this.identifiers = Objects.requireNonNullElse(identifiers, List.of());
    }

    public static StatementLineReplicaProcessorSupplier replicate(final String... identifiers) {
        return new StatementLineReplicaProcessorSupplier(Arrays.asList(identifiers));
    }

    @Override
    public FixedKeyProcessor<UUID, StatementLine, StatementLine> get() {
        return new StatementLineReplicaProcessor();
    }

    private class StatementLineReplicaProcessor extends ContextualFixedKeyProcessor<UUID, StatementLine, StatementLine> {

        @Override
        public void process(final FixedKeyRecord<UUID, StatementLine> record) {

            final String operation = UUID
                    .randomUUID()
                    .toString();

            final StatementLine originalStatementLine = record.value();
            final Map<String, String> originalBaggage = originalStatementLine.getBaggage();

            identifiers.forEach(id -> {

                final Map<String, String> replicatedBaggage = new HashMap<>(originalBaggage);
                replicatedBaggage.put(REPLICA_ID_KEY, id);
                replicatedBaggage.put(REPLICA_OPERATION_KEY, operation);

                final StatementLine replicatedLine = StatementLine.newBuilder(originalStatementLine)
                        .setBaggage(replicatedBaggage)
                        .build();

                final FixedKeyRecord<UUID, StatementLine> replicatedRecord = record.withValue(replicatedLine);
                context().forward(replicatedRecord);

            });
        }
    }
}
