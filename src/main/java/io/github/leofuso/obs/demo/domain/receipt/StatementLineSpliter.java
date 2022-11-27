package io.github.leofuso.obs.demo.domain.receipt;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

import org.apache.kafka.streams.processor.api.Record;
import org.apache.logging.log4j.util.Strings;

import io.github.leofuso.obs.demo.events.StatementLine;

public final class StatementLineSpliter {

    private static final Pattern PATTERN = Pattern.compile(
            "(?im)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
    );

    private static final String BAGGAGE_ORDER_KEY = "orders";

    public Iterable<Record<UUID, StatementLine>> byOrder(final Record<UUID, StatementLine> lineRecord) {

        final StatementLine line = lineRecord.value();
        final Map<String, String> baggage = line.getBaggage();

        final String orders = baggage.getOrDefault(BAGGAGE_ORDER_KEY, Strings.EMPTY);
        final Matcher matcher = PATTERN.matcher(orders);

        final Iterator<String> orderIterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return matcher.find();
            }

            @Override
            public String next() {
                return matcher.group();
            }
        };

        final Spliterator<String> spliterator = Spliterators.spliteratorUnknownSize(
                orderIterator,
                Spliterator.NONNULL | Spliterator.IMMUTABLE
        );

        return StreamSupport.stream(spliterator, false)
                            .distinct()
                            .map(UUID::fromString)
                            .map(lineRecord::withKey)
                            .toList();
    }
}
