package io.github.leofuso.obs.demo.domain.apportionment.baggage;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.util.Strings;

import io.github.leofuso.obs.demo.events.StatementLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaggageOrderExtractor {

    private static final Logger logger = LoggerFactory.getLogger(BaggageOrderExtractor.class);

    private static BaggageOrderExtractor INTANCE;

    private BaggageOrderExtractor() {}

    public static BaggageOrderExtractor getInstance() {
        if (INTANCE == null) {
            INTANCE = new BaggageOrderExtractor();
        }
        return INTANCE;
    }

    public List<UUID> extract(final StatementLine line) {
        final Spliterator<UUID> spliterator = Spliterators.spliteratorUnknownSize(
                new UUIDIterator(line),
                Spliterator.NONNULL | Spliterator.IMMUTABLE
        );
        return StreamSupport.stream(spliterator, false)
                .distinct()
                .collect(Collectors.toList());
    }

    private static class UUIDIterator implements Iterator<UUID> {

        private static final Pattern PATTERN = Pattern.compile(
                "(?im)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        );

        private static final String BAGGAGE_ORDER_KEY = "orders";

        private final Matcher matcher;

        private UUIDIterator(final StatementLine line) {
            final Map<String, String> baggage = line.getBaggage();
            final String orders = baggage.getOrDefault(BAGGAGE_ORDER_KEY, Strings.EMPTY);
            this.matcher = PATTERN.matcher(orders);
        }

        @Override
        public boolean hasNext() {
            return matcher.find();
        }

        @Override
        public UUID next() {
            try {
                final String group = matcher.group();
                return UUID.fromString(group);
            } catch (IllegalStateException ex) {
                logger.error("Critical error extracint Order ID: ", ex);
                return new UUID(0, 0);
            }
        }
    }

}
