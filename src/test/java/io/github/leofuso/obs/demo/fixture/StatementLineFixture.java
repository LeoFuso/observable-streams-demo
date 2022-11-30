package io.github.leofuso.obs.demo.fixture;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.params.provider.*;

import io.github.leofuso.obs.demo.events.*;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class StatementLineFixture {

    static Stream<Arguments> routeAndIncentives(final UUID uuid, final StatementLine template) {
        return Stream.of(
                arguments(uuid, template),
                arguments(uuid, template)
        );
    }

}
