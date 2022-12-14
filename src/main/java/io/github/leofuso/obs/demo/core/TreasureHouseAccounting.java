package io.github.leofuso.obs.demo.core;

import java.util.*;
import java.util.function.*;

import org.springframework.context.annotation.*;

import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.*;

import io.github.leofuso.obs.demo.domain.branch.*;
import io.github.leofuso.obs.demo.events.*;

import jakarta.annotation.*;

import static io.github.leofuso.obs.demo.core.configuration.TopicConfiguration.*;

@Configuration
public class TreasureHouseAccounting {

    private static final Logger logger = LoggerFactory.getLogger(StatementLineApportionment.class);

    private final StreamsBuilder streamsBuilder;

    public TreasureHouseAccounting(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required");
    }

    @Bean
    public TreasureHouseAccountingBranch treasureHouseAccountingBranch() {
        return StatementLineBranch.produce(TreasureHouseAccountingBranch.class);
    }

    @PostConstruct
    public void setup() {

        final Function<String, String> name = "stln-accounting-%s"::formatted;
        streamsBuilder.<UUID, StatementLine>stream(TREASURE_HOUSE_ACCOUNTING_BRANCH, Consumed.as(name.apply("consumer")))
                .peek(
                        (key, value) -> {
                            final String message = """
                                    Processing accounting for StatementLine [{}]""";
                            logger.debug(message, key);
                        },
                        Named.as(name.apply("peek"))
                );
    }

}
