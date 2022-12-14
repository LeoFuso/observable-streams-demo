package io.github.leofuso.obs.demo.core;

import java.util.*;
import java.util.function.*;

import org.apache.kafka.streams.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import io.github.leofuso.obs.demo.core.configuration.*;
import io.github.leofuso.obs.demo.events.*;
import io.github.leofuso.obs.demo.fixture.annotation.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@DisplayName("StatementLinesClassifier core tests")
class StatementLinesClassifierTest extends CoreTest {

    private TestInputTopic<UUID, StatementLine> source;
    private TestOutputTopic<UUID, StatementLine> apportionmentBranch;
    private TestOutputTopic<UUID, StatementLine> treasureBranch;

    @Override
    protected void contextSetup() {
        source = topicFixture.input(TopicConfiguration.APPROVED_STATEMENT_LINE);
        apportionmentBranch = topicFixture.output(TopicConfiguration.STATEMENT_LINE_APPORTIONMENT_RECEIPT_LINE_REPARTITION);
        treasureBranch = topicFixture.output(TopicConfiguration.TREASURE_HOUSE_ACCOUNTING_BRANCH);
    }

    @DisplayName(
            """
                     Given a StatementLine that may or may not be supported by StatementLineApportionment,
                     when classifying,
                     then redirects only supported StatementLine
                    """
    )
    @EnumSource
    @ParameterizedTest(name = "{index} - {0}")
    void b8aec7e506ce410bb646f517cf71784c(Department department, @RecordParameter("statement-line.template.json") StatementLine statement) {

        /* Given */
        final UUID key = statement.getTransaction();
        final Details details = statement.getDetails();
        details.setDepartment(department);

        final Set<Department> supportedDepartments = Set.of(Department.ROUTE, Department.INCENTIVE);
        final BooleanSupplier supportedDepartmentAssumption = () -> supportedDepartments.contains(department);
        final BooleanSupplier unsupportedDepartmentAssumption = () -> !supportedDepartments.contains(department);

        /* When */
        source.pipeInput(key, statement);

        /* Then*/
        assumingThat(
                supportedDepartmentAssumption,
                () -> assertThat(apportionmentBranch.readKeyValue())
                        .isNotNull()
                        .extracting(kv -> kv.key)
                        .isEqualTo(key)
        );
        assumingThat(
                unsupportedDepartmentAssumption,
                () -> assertThat(apportionmentBranch.isEmpty()).isTrue()
        );
    }

    @DisplayName(
            """
                     Given a StatementLine that may or may not be supported by TreasureHouseAccounting,
                     when classifying,
                     then redirects only supported StatementLine
                    """
    )
    @EnumSource
    @ParameterizedTest(name = "{index} - {0}")
    void a62661d6d9284b408647adac87aaea32(Department department, @RecordParameter("statement-line.template.json") StatementLine statement) {

        /* Given */
        final UUID key = statement.getTransaction();
        final Details details = statement.getDetails();
        details.setDepartment(department);

        /* When */
        source.pipeInput(key, statement);

        /* Then*/
        final KeyValue<UUID, StatementLine> keyValue = treasureBranch.readKeyValue();
        assertThat(keyValue)
                .isNotNull()
                .extracting(kv -> kv.key)
                .isEqualTo(key);
    }
}