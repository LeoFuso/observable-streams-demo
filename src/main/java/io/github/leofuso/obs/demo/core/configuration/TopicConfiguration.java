package io.github.leofuso.obs.demo.core.configuration;

import java.time.*;
import java.util.*;

import org.springframework.context.annotation.*;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;

/**
 * A {@link Configuration} helper class for all needed topics in the Application.
 */
@Configuration
public class TopicConfiguration {

    /**
     * The topic holder of all approved {@link io.github.leofuso.obs.demo.events.StatementLine Statement Line} events.
     */
    public static final String APPROVED_STATEMENT_LINE = "obs.approved-statement-lines";

    public static final String STATEMENT_LINE_APPORTIONMENT_BRANCH = "obs.statement-line-apportionment-branch";

    public static final String RECEIPT_CHANGELOG = "obs.internal-receipt-store-changelog";

    public static final String RECEIPT_STORE = "receipt-store";

    public static final String TREASURE_HOUSE_ACCOUNTING_BRANCH = "obs.treasure-house-accouting-branch";

    /**
     * All Kafka's Topics work with the same quantity of partitions. The five-partition amount was chosen arbitrarily.
     */
    private static final Integer PARTITIONS_COUNT = 5;

    @Bean
    public KafkaAdmin.NewTopics topics() {

        final long oneHour = Duration
                .ofHours(1)
                .toMillis();

        return new KafkaAdmin.NewTopics(
                TopicBuilder
                        .name(APPROVED_STATEMENT_LINE)
                        .partitions(PARTITIONS_COUNT)
                        .configs(
                                /* @formatter:off */
                                Map.of(
                                        "retention.ms", oneHour + ""
                                )
                                /* @formatter:on */
                        )
                        .build(),
                TopicBuilder
                        .name(STATEMENT_LINE_APPORTIONMENT_BRANCH)
                        .partitions(PARTITIONS_COUNT)
                        .configs(
                                /* @formatter:off */
                                Map.of(
                                        "retention.ms", oneHour + ""
                                )
                                /* @formatter:on */
                        )
                        .build(),
                TopicBuilder
                        .name(TREASURE_HOUSE_ACCOUNTING_BRANCH)
                        .partitions(PARTITIONS_COUNT)
                        .configs(
                                /* @formatter:off */
                                Map.of(
                                        "retention.ms", oneHour + ""
                                )
                                /* @formatter:on */
                        )
                        .build()
        );
    }

}
