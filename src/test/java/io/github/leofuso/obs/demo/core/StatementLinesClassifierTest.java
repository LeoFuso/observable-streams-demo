package io.github.leofuso.obs.demo.core;

import java.util.*;

import org.apache.kafka.streams.*;
import org.junit.jupiter.api.*;

import io.github.leofuso.obs.demo.core.configuration.*;
import io.github.leofuso.obs.demo.events.*;

class StatementLinesClassifierTest extends CoreTest {

    private TestInputTopic<UUID, StatementLine> source;
    private TestInputTopic<UUID, StatementLine> apportionmentBranch;

    @Override
    protected void contextSetup() {
        source = topicFixture.input(TopicConfiguration.APPROVED_STATEMENT_LINE);
        apportionmentBranch = topicFixture.input(TopicConfiguration.STATEMENT_LINE_APPORTIONMENT_BRANCH);
    }


    @Test
    @DisplayName(
            """
                    Given
                    When
                    Then
                    """
    )
    void b8aec7e506ce410bb646f517cf71784c() {

        /* Given */
        final StatementLine statementLine = loadRecord("events/statement-line/template.json", StatementLine.class);

        /* When */

        /* Then*/

    }

}