package io.github.leofuso.obs.demo.core;

import java.util.*;

import org.apache.kafka.streams.*;
import org.apache.kafka.streams.state.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import io.github.leofuso.obs.demo.core.configuration.*;
import io.github.leofuso.obs.demo.events.*;
import io.github.leofuso.obs.demo.fixture.*;

@DisplayName("StatementLineApportionment core tests")
@ExtendWith({ StatementLineParameterResolver.class, UUIDParameterResolver.class })
class StatementLineApportionmentTest extends CoreTest {

    private TestInputTopic<UUID, StatementLine> source;
    private WindowStore<UUID, Receipt> store;

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
    @EnumSource(names = { "ROUTE", "INCENTIVE" })
    void bbb3cb2a2c1743059a7df8beeff00bbd(final Department department, final UUID uuid, final StatementLine line) {

        /* Given */
        final Receipt receipt = loadRecord("events/receipt/template.json", Receipt.class);

        /* When */

        /* Then */
    }
}