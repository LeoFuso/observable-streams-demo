package io.github.leofuso.obs.demo.fixture;

import io.github.leofuso.obs.demo.events.Source;
import io.github.leofuso.obs.demo.events.StatementLine;
import org.apache.kafka.streams.KeyValue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class StatementLineFixture {

    public static final String domain = "obs.demo";

    public static Stream<KeyValue<UUID, StatementLine>> generate(StatementLine template, BigDecimal upperBound, UUID[]... packs) {

        final long millis = System.currentTimeMillis();
        final Random random = new Random(millis);

        /* There's probably a better way of doing this, oh well... */
        final int initialValue = (int) (upperBound.doubleValue() * (Math.pow(10, upperBound.scale())));
        final AtomicInteger sharePool = new AtomicInteger(initialValue);
        return stream(packs, 0, packs.length)
                .map(orders -> {

                    final UUID namespace = UUID.randomUUID();
                    final UUID key = UUIDFixture.fromNamespaceAndBytes(namespace, domain.getBytes());

                    final int found = random.nextInt(sharePool.get());
                    sharePool.addAndGet(-found);
                    final BigDecimal amount = BigDecimal.valueOf(found, 3);
                    return StatementLine.newBuilder(template)
                            .setTransaction(key)
                            .setSource(
                                    Source.newBuilder()
                                            .setDomain(domain)
                                            .setNamespace(namespace)
                                            .build()
                            )
                            .setAmount(amount)
                            .setBaggage(baggage(orders))
                            .build();
                })
                .map(line -> KeyValue.pair(line.getTransaction(), line));
    }

    private static Map<String, String> baggage(UUID... orders) {
        if (orders.length == 0) {
            return Map.of();
        }
        return Map.of(
                "orders", "%s".formatted(
                        stream(orders, 0, orders.length)
                                .map(UUID::toString)
                                .collect(Collectors.joining(", "))
                )
        );
    }

}
