package io.github.leofuso.obs.demo.core;

import java.util.Objects;

import org.apache.kafka.streams.StreamsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.leofuso.obs.demo.domain.branch.*;

@Configuration
public class TreasureHouseAccounting {

    private static final Logger logger = LoggerFactory.getLogger(StatementLineApportionment.class);

    private static final String NAMED_SUFFIX = "stln-accounting";

    private final StreamsBuilder streamsBuilder;

    public TreasureHouseAccounting(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required.");
    }

    @Bean
    public TreasureHouseAccoutingBranch treasureHouseAccountingBranch() {
        return StatementLineBranch.produce(TreasureHouseAccoutingBranch.class);
    }

    @Bean
    public UnknownDepartmentBranch unknownDepartmentBranch() {
        return StatementLineBranch.produce(UnknownDepartmentBranch.class);
    }



}
