package io.github.leofuso.obs.demo.core.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.time.Duration;
import java.util.Map;

/**
 * A {@link Configuration} helper class for all needed topics in the Application.
 */
@Configuration
public class TopicConfiguration {

    /**
     * The topic holder of all approved {@link io.github.leofuso.obs.demo.events.StatementLine Statement Line} events.
     */
    public static final String APPROVED_STATEMENT_LINE = "obs.approved-statement-lines";

    /**
     * The topic holding all {@link io.github.leofuso.obs.demo.events.StatementLine Statement Line} events waiting for the apportionment process.
     */
    public static final String STATEMENT_LINE_APPORTIONMENT_BRANCH = "obs.statement-line-apportionment-branch";

    /**
     * The topic holding all {@link io.github.leofuso.obs.demo.events.Receipt Receipts}.
     */
    public static final String RECEIPT = "obs.receipt";

    /**
     * The {@link io.github.leofuso.obs.demo.events.Receipt Receipt} store name.
     */
    public static final String RECEIPT_STORE = "receipt-store";

    /**
     * The {@link io.github.leofuso.obs.demo.events.Receipt Receipt} store changelog topic.
     */
    public static final String RECEIPT_CHANGELOG = "obs.internal-receipt-store-changelog";

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
                        .build(),
                TopicBuilder
                        .name(RECEIPT)
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
