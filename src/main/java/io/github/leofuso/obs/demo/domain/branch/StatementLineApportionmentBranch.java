package io.github.leofuso.obs.demo.domain.branch;

import java.util.*;

import org.apache.kafka.streams.kstream.*;

import io.github.leofuso.obs.demo.events.*;

import static io.github.leofuso.obs.demo.domain.branch.StatementLineReplicaProcessorSupplier.REPLICA_ID_KEY;

public class StatementLineApportionmentBranch implements StatementLineBranch {

    private static final Set<Department> SUPPORTED = Set.of(
            Department.ROUTE,
            Department.INCENTIVE
    );

    private final String cachedName;

    public StatementLineApportionmentBranch() {
        cachedName = StatementLineBranch.super.name();
    }

    @Override
    public Predicate<UUID, StatementLine> supports() {
        return (key, value) -> {

            final Details details = value.getDetails();
            final Department department = details.getDepartment();

            return SUPPORTED.contains(department)
                    && Optional.of(value)
                    .map(StatementLine::getBaggage)
                    .map(baggage -> baggage.get(REPLICA_ID_KEY))
                    .map(id -> name().equals(id))
                    .orElse(false);
        };
    }

    @Override
    public String name() {
        return cachedName;
    }
}
