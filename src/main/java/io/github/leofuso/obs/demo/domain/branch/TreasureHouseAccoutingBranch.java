package io.github.leofuso.obs.demo.domain.branch;

import java.util.Optional;
import java.util.UUID;

import org.apache.kafka.streams.kstream.Predicate;

import io.github.leofuso.obs.demo.events.StatementLine;

import static io.github.leofuso.obs.demo.domain.branch.StatementLineReplicaProcessorSupplier.REPLICA_ID_KEY;

public class TreasureHouseAccoutingBranch implements StatementLineBranch {

    private final String cachedName;

    private TreasureHouseAccoutingBranch() {
        cachedName = StatementLineBranch.super.name();
    }

    @Override
    public Predicate<UUID, StatementLine> supports() {
        return (key, value) -> Optional
                .ofNullable(value)
                .map(StatementLine::getBaggage)
                .map(baggage -> baggage.get(REPLICA_ID_KEY))
                .map(id -> name().equals(id))
                .orElse(false);
    }

    @Override
    public String name() {
        return cachedName;
    }
}
