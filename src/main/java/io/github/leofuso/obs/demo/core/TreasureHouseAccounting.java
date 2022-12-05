package io.github.leofuso.obs.demo.core;

import io.github.leofuso.obs.demo.domain.branch.StatementLineBranch;
import io.github.leofuso.obs.demo.domain.branch.TreasureHouseAccoutingBranch;
import io.github.leofuso.obs.demo.events.StatementLine;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static io.github.leofuso.obs.demo.core.configuration.TopicConfiguration.TREASURE_HOUSE_ACCOUNTING_BRANCH;

@Configuration
public class TreasureHouseAccounting {

    private static final Logger logger = LoggerFactory.getLogger(StatementLineApportionment.class);

    private final StreamsBuilder streamsBuilder;

    public TreasureHouseAccounting(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required");
    }

    @Bean
    public TreasureHouseAccoutingBranch treasureHouseAccountingBranch() {
        return StatementLineBranch.produce(TreasureHouseAccoutingBranch.class);
    }

    @PostConstruct
    public void setup() {

        final Function<String, String> name = "stln-accounting-%s"::formatted;
        streamsBuilder.<UUID, StatementLine>stream(TREASURE_HOUSE_ACCOUNTING_BRANCH, Consumed.as(name.apply("consumer")))
                .peek((key, value) -> {
                            final String message = """
                                    Processing accounting for StatementLine [{}]""";
                            logger.debug(message, key);
                        },
                        Named.as(name.apply("peek"))
                );
    }

}
