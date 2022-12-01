package io.github.leofuso.obs.demo.core;

import java.util.*;
import java.util.stream.*;

import org.apache.kafka.streams.*;
import org.apache.kafka.streams.state.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import io.github.leofuso.obs.demo.core.configuration.*;
import io.github.leofuso.obs.demo.events.*;
import io.github.leofuso.obs.demo.fixture.*;
import io.github.leofuso.obs.demo.fixture.annotation.*;

@DisplayName("StatementLineApportionment core tests")
class StatementLineApportionmentTest extends CoreTest {

    private TestInputTopic<UUID, StatementLine> source;
    private WindowStore<UUID, Receipt> store;

    static Stream<Arguments> receipt(@RecordParameter("statement-line.template.json") StatementLine template) {

        final String name = "obs.demo";
        final UUID order = UUID.randomUUID();

        return IntStream.range(0, 3)
                .mapToObj(i -> {

                    final UUID namespace = UUID.randomUUID();
                    final UUID key = UUIDFixture.fromNamespaceAndBytes(namespace, name.getBytes());

                    return StatementLine.newBuilder(template)
                            .setTransaction(key)
                            .setBaggage(Map.of(
                                    "orders", "%s".formatted(order)
                            ))
                            .build();
                })
                .map(Arguments::arguments);
    }

    @Override
    protected void contextSetup() {
        source = topicFixture.input(TopicConfiguration.STATEMENT_LINE_APPORTIONMENT_BRANCH);
        store = testDriver.getWindowStore(TopicConfiguration.RECEIPT_STORE);
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
    void bbb3cb2a2c1743059a7df8beeff00bbd(Iterable<StatementLine> lines) {

        /* Given */
        System.out.println(lines);

        /* When */

        /* Then */
    }


}