package io.github.leofuso.obs.demo.domain.branch;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.leofuso.obs.demo.events.*;

public class UnknownDepartmentBranch implements StatementLineBranch {

    private static final Logger logger = LoggerFactory.getLogger(UnknownDepartmentBranch.class);

    private static final TreasureHouseAccoutingBranch DEFAULT_BRANCH =
            StatementLineBranch.produce(TreasureHouseAccoutingBranch.class);

    @Override
    public Predicate<UUID, StatementLine> supports() {
        return (key, value) -> {
            final Details details = value.getDetails();
            final Department department = details.getDepartment();
            return Department.UNKNOWN == department;
        };
    }

    @Override
    public Branched<UUID, StatementLine> branched() {

        final Named namedWarning = Named.as(name() + "-warning");
        final Produced<UUID, StatementLine> produced = Produced.as(name() + "-produced");

        final Consumer<KStream<UUID, StatementLine>> kStreams =
                kStream -> kStream.peek(
                                (key, value) -> {
                                    final String message =
                                            """
                                                    Skipping apportionment [ {} ]: Unknown department.
                                                    """;
                                    logger.warn(message, key);
                                },
                                namedWarning
                        )
                        .to(topic(), produced);

        final String branchedName = "-" + StatementLineBranch.namedRouter(this) + "-branch";
        return Branched.withConsumer(kStreams, branchedName);
    }

    @Override
    public String topic() {
        return DEFAULT_BRANCH.topic();
    }
}
