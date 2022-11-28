package io.github.leofuso.obs.demo.domain.branch;

import java.util.Set;
import java.util.UUID;

import org.apache.kafka.streams.kstream.Predicate;

import com.google.common.collect.Sets;

import io.github.leofuso.obs.demo.events.*;

public class TreasureHouseAccoutingBranch implements StatementLineBranch {

    private static final Set<Department> UNSUPPORTED = Set.of(
            Department.ROUTE,
            Department.INCENTIVE
    );

    private static final Set<Department> SUPPORTED = Sets.complementOf(UNSUPPORTED);

    @Override
    public Predicate<UUID, StatementLine> supports() {
        return (key, value) -> {
            final Details details = value.getDetails();
            final Department department = details.getDepartment();
            return SUPPORTED.contains(department);
        };
    }
}
