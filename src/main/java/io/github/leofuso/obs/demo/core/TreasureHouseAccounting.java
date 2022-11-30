package io.github.leofuso.obs.demo.core;

import java.util.*;

import org.springframework.context.annotation.*;

import org.apache.kafka.streams.*;
import org.slf4j.*;

import io.github.leofuso.obs.demo.domain.branch.*;

@Configuration
public class TreasureHouseAccounting {

    private static final Logger logger = LoggerFactory.getLogger(StatementLineApportionment.class);

    private static final String NAMED_SUFFIX = "stln-accounting";

    private final StreamsBuilder streamsBuilder;

    public TreasureHouseAccounting(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required");
    }

    @Bean
    public TreasureHouseAccoutingBranch treasureHouseAccountingBranch() {
        return StatementLineBranch.produce(TreasureHouseAccoutingBranch.class);
    }

}
