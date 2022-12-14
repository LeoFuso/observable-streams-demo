package io.github.leofuso.obs.demo.core;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.apache.kafka.streams.*;
import org.assertj.core.api.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import io.github.leofuso.obs.demo.core.configuration.*;
import io.github.leofuso.obs.demo.events.*;
import io.github.leofuso.obs.demo.fixture.*;
import io.github.leofuso.obs.demo.fixture.annotation.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Named.*;
import static org.junit.jupiter.params.provider.Arguments.*;

@DisplayName("StatementLineApportionment core tests")
class StatementLineApportionmentTest extends CoreTest {

    private TestInputTopic<UUID, StatementLine> source;
    private TestOutputTopic<UUID, Receipt> output;

    static Stream<Arguments> receipt(@RecordParameter("statement-line.template.json") StatementLine template) {

        final UUID order_A = UUID.fromString("f1a4cfc3-b1a2-459d-ab0c-d7ac3cac42d0");
        final UUID order_B = UUID.fromString("126cefe4-aab4-4bb6-9876-e245c834b0ac");
        final UUID order_C = UUID.fromString("26712a78-ca93-4a9f-aa72-25d08adfff64");
        final UUID order_D = UUID.fromString("4475ad68-5c74-40bb-ae82-c79dfab3f149");
        final UUID order_E = UUID.fromString("e6cd74f0-3918-4e3d-9d81-1c05deccd263");

        return Stream.of(
                arguments(
                        named(
                                "A collection with 7 StatementLines",
                                Stream.of(
                                                StatementLineFixture.generate(
                                                        template,
                                                        BigDecimal.valueOf(35.579),
                                                        new UUID[] { order_A, order_B },
                                                        new UUID[] { order_C, order_D },
                                                        new UUID[] { order_E }
                                                ),
                                                StatementLineFixture.generate(
                                                        template,
                                                        BigDecimal.valueOf(12.299),
                                                        new UUID[] { order_C },
                                                        new UUID[] { order_A },
                                                        new UUID[] { order_B }
                                                ), StatementLineFixture.generate(
                                                        template,
                                                        BigDecimal.valueOf(8.370),
                                                        new UUID[] { order_A, order_E }
                                                )
                                        )
                                        .flatMap(s -> s)
                                        .toList()
                        ),
                        named("A Map of Order-UpperBound", Map.of(
                                order_A, BigDecimal.valueOf(35.579 + 12.299 + 8.370),
                                order_B, BigDecimal.valueOf(35.579 + 12.299),
                                order_C, BigDecimal.valueOf(35.579 + 12.299),
                                order_D, BigDecimal.valueOf(35.579)
                        )),
                        named("Expected step", Duration.ofMinutes(1))
                ),
                arguments(
                        named("A collection with 4 StatementLines", Stream.of(
                                        StatementLineFixture.generate(
                                                template,
                                                BigDecimal.valueOf(10.500),
                                                new UUID[] { order_A, order_E },
                                                new UUID[] { order_C, order_D }
                                        ),
                                        StatementLineFixture.generate(
                                                template,
                                                BigDecimal.valueOf(15.299),
                                                new UUID[] { order_E },
                                                new UUID[] { order_C, order_D }
                                        )
                                )
                                .flatMap(stream -> stream)
                                .toList()),
                        named("A Map of Order-Sums", Map.of(
                                order_A, BigDecimal.valueOf(10.500),
                                order_E, BigDecimal.valueOf(10.500 + 15.299)
                        )),
                        named("Expected step", Duration.ofMinutes(2))
                )
        );
    }

    @Override
    protected void contextSetup() {
        source = topicFixture.input(TopicConfiguration.STATEMENT_LINE_APPORTIONMENT_RECEIPT_LINE_REPARTITION);
        output = topicFixture.output(TopicConfiguration.RECEIPT);
    }

    @DisplayName(
            """
                     Given some StatementLines,
                     when performing all apportionments,
                     then build a Receipt
                    """
    )
    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("receipt")
    void bbb3cb2a2c1743059a7df8beef00bbd(List<KeyValue<UUID, StatementLine>> keyValues, Map<UUID, BigDecimal> upperBound, Duration step) {

        /* Given */
        final Instant start = Instant.parse("2022-12-05T01:18:00.693309073Z");

        /* When */
        source.pipeKeyValueList(
                keyValues,
                start,
                step
        );

        /* Then */
        final Map<UUID, Receipt> receipts = output.readKeyValuesToMap();
        upperBound.forEach((uuid, deficit) ->
                                   assertThat(receipts)
                                           .asInstanceOf(InstanceOfAssertFactories.map(UUID.class, Receipt.class))
                                           .hasEntrySatisfying(
                                                   uuid,
                                                   receipt -> assertThat(receipt)
                                                           .extracting(Receipt::getDeficit)
                                                           .asInstanceOf(InstanceOfAssertFactories.BIG_DECIMAL)
                                                           .isLessThanOrEqualTo(deficit)
                                           )
        );
    }
}