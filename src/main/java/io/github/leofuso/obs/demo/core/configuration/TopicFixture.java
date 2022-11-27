package io.github.leofuso.obs.demo.core.configuration;

import java.time.Duration;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * A {@link Configuration} helper class for all needed topics in the Application.
 */
@Configuration
public class TopicFixture {

    /**
     * The topic holder of all approved {@link io.github.leofuso.obs.demo.events.StatementLine Statement Line} events.
     */
    public static final String APPROVED_STATEMENT_LINE = "obs.approved-statement-lines";

    public static final String RECEIPT_APPORTIONMENT_ROUTE = "obs.internal-receipt-apportionment-router";

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
                        .name(APPROVED_STATEMENT_LINE)
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
