package io.github.leofuso.obs.demo.domain.branch;

import java.util.*;

import org.apache.kafka.streams.kstream.*;

import io.github.leofuso.obs.demo.events.*;

import static io.github.leofuso.obs.demo.domain.branch.StatementLineReplicaProcessorSupplier.REPLICA_ID_KEY;

public class TreasureHouseAccoutingBranch implements StatementLineBranch {

    private final String cachedName;

    public TreasureHouseAccoutingBranch() {
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
