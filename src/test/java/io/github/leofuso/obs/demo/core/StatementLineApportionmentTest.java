package io.github.leofuso.obs.demo.core;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.*;
import org.assertj.core.api.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import io.github.leofuso.obs.demo.core.configuration.*;
import io.github.leofuso.obs.demo.events.*;
import io.github.leofuso.obs.demo.fixture.*;
import io.github.leofuso.obs.demo.fixture.annotation.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StatementLineApportionment core tests")
class StatementLineApportionmentTest extends CoreTest {

    private TestInputTopic<UUID, StatementLine> source;
    private WindowStore<UUID, ValueAndTimestamp<Receipt>> store;

    static Stream<Arguments> receipt(@RecordParameter("statement-line.template.json") StatementLine template) {

        final String name = "obs.demo";
        final UUID order = UUID.randomUUID();

        final long millis = System.currentTimeMillis();
        final Random random = new Random(millis);

        final AtomicInteger upperBound = new AtomicInteger(40000);
        final List<StatementLine> lines = IntStream.range(0, 3)
                .mapToObj(i -> {

                    final UUID namespace = UUID.randomUUID();
                    final UUID key = UUIDFixture.fromNamespaceAndBytes(namespace, name.getBytes());

                    final int found = random.nextInt(upperBound.get());
                    upperBound.addAndGet(-found);
                    final BigDecimal amount = BigDecimal.valueOf(found, 3);
                    return StatementLine.newBuilder(template)
                            .setTransaction(key)
                            .setAmount(amount)
                            .setBaggage(
                                    Map.of(
                                            "orders", "%s%s".formatted(
                                                    order,
                                                    IntStream.range(0, i)
                                                            .mapToObj(dividers -> UUID.randomUUID())
                                                            .map(Object::toString)
                                                            .collect(
                                                                    Collectors.joining(", ", ", ", "")
                                                            )
                                            )
                                    ))
                            .build();
                })
                .toList();

        return Stream.of(Arguments.of(lines));
    }

    @Override
    protected void contextSetup() {
        source = topicFixture.input(TopicConfiguration.STATEMENT_LINE_APPORTIONMENT_BRANCH);
        store = testDriver.getTimestampedWindowStore(TopicConfiguration.RECEIPT_STORE);
    }

    @DisplayName(
            """
                     Given a StatementLine that may or may not be supported by StatementLineApportionment,
                     when classifying,
                     then redirects only supported StatementLine
                    """
    )
    @ParameterizedTest
    @MethodSource("receipt")
    void bbb3cb2a2c1743059a7df8beeff00bbd(Iterable<StatementLine> statementLines) {

        /* Given */
        final Instant startpoint = Instant.now();
        final Duration step = Duration.ofMinutes(2);

        /* When */
        Instant punctuate = startpoint.plusSeconds(10);
        for (final StatementLine statement : statementLines) {

            final Clock fixedClock = Clock.fixed(punctuate, ZoneId.systemDefault());
            final Instant timestamp = Instant.now(fixedClock);

            source.pipeInput(statement.getTransaction(), statement, timestamp);
            testDriver.advanceWallClockTime(step);

            punctuate = punctuate.plus(step);
        }

        testDriver.advanceWallClockTime(step);
        punctuate = punctuate.plus(step);

        /* Then */
        final KeyValueIterator<Windowed<UUID>, ValueAndTimestamp<Receipt>> receipts = store.fetchAll(startpoint, punctuate);
        final List<KeyValue<Windowed<UUID>, ValueAndTimestamp<Receipt>>> values = Stream
                .iterate(
                        receipts.next(),
                        kv -> receipts.hasNext(),
                        kv -> receipts.next()
                )
                .toList();


        assertThat(receipts)
                .asList()
                .singleElement()
                .asInstanceOf(InstanceOfAssertFactories.type(KeyValue.class))
                .satisfies(kv -> {

                    final Windowed<UUID> key = (Windowed<UUID>) kv.key;
                    final Receipt value = (Receipt) kv.value;

                    assertThat(value)
                            .satisfies(receipt -> {

                                assertThat(receipt)
                                        .extracting(Receipt::getOrder)
                                        .isEqualTo(key.key());

                                assertThat(receipt)
                                        .extracting(Receipt::getLines)
                                        .asInstanceOf(InstanceOfAssertFactories.map(String.class, StatementLine.class))
                                        .hasSize(3);
                            });
                });
    }


}