package io.github.leofuso.obs.demo.domain.apportionment;

import io.github.leofuso.obs.demo.domain.apportionment.baggage.BaggageOrderExtractor;
import io.github.leofuso.obs.demo.events.Ratio;
import io.github.leofuso.obs.demo.events.Receipt;
import io.github.leofuso.obs.demo.events.ReceiptLine;
import io.github.leofuso.obs.demo.events.StatementLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReceiptFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptFactory.class);

    private static ReceiptFactory INSTANCE;
    private final BaggageOrderExtractor baggageSolver;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

    private ReceiptFactory() {
        baggageSolver = BaggageOrderExtractor.getInstance();
    }

    public static ReceiptFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReceiptFactory();
        }
        return INSTANCE;
    }

    public static Receipt identity() {
        return Receipt.newBuilder()
                .setDeficit(BigDecimal.ZERO)
                .setOrder(new UUID(0, 0))
                .setLines(Map.of())
                .build();
    }

    public static Receipt aggregate(final UUID key, final ReceiptLine line, final Receipt receipt) {

        final UUID transaction = line.getObject()
                .getTransaction();

        final Map<String, ReceiptLine> lines = new HashMap<>(receipt.getLines());
        lines.merge(transaction + "", line, (original, repeated) -> {
            final String message = """
                    Duplicated Receipt line [{}] ignored.
                    """;
            logger.warn(message, transaction);
            return original;
        });

        final BigDecimal lineShare = line.getShare();
        final BigDecimal currentDeficit = receipt.getDeficit();
        final BigDecimal aggregateDeficit = currentDeficit.add(lineShare);

        return Receipt.newBuilder()
                .setOrder(key)
                .setLines(Map.copyOf(lines))
                .setDeficit(aggregateDeficit)
                .setBaggage(Map.copyOf(receipt.getBaggage()))
                .build();
    }

    public Map<UUID, ReceiptLine> performApportion(final StatementLine object) {

        final List<UUID> orders = baggageSolver.extract(object);
        if (orders.isEmpty()) {
            return Map.of();
        }

        final BigDecimal amount = object.getAmount();
        final int denominator = orders.size();

        final BigDecimal divisor = BigDecimal.valueOf(denominator);
        final BigDecimal share = amount.divide(divisor, RoundingMode.HALF_EVEN);

        final Ratio ratio = Ratio.newBuilder()
                .setNumerator(1)
                .setDenominator(denominator)
                .build();

        final ReceiptLine receiptLine = ReceiptLine.newBuilder()
                .setShare(share)
                .setRatio(ratio)
                .setObject(object)
                .build();

        return orders.stream()
                .peek(order -> {
                    final String message =
                            """
                                    Found StatementLine apportionment [{}] of Order [{}] to be [{}]""";

                    logger.debug(message, object.getTransaction(), order, currencyFormatter.format(share));
                })
                .collect(Collectors.toMap(Function.identity(), ignored -> receiptLine));
    }
}
