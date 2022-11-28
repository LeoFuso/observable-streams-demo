package io.github.leofuso.obs.demo.domain.branch;

import java.util.Set;
import java.util.UUID;

import org.apache.kafka.streams.kstream.Predicate;

import io.github.leofuso.obs.demo.events.*;

public class StatementLineApportionmentBranch implements StatementLineBranch {

    private static final Set<Department> SUPPORTED = Set.of(
            Department.ROUTE,
            Department.INCENTIVE
    );

    @Override
    public Predicate<UUID, StatementLine> supports() {
        return (key, value) -> {
            final Details details = value.getDetails();
            final Department department = details.getDepartment();
            return SUPPORTED.contains(department);
        };
    }
}
