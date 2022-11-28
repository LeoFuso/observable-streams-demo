package io.github.leofuso.obs.demo.domain.apportionment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.leofuso.obs.demo.domain.apportionment.baggage.BaggageOrderExtractor;
import io.github.leofuso.obs.demo.events.*;

public class ReceiptFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptFactory.class);

    private static ReceiptFactory INSTANCE;
    private final BaggageOrderExtractor baggageSolver;

    public ReceiptFactory() {
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
                .setLines(new HashMap<>())
                .build();
    }

    public static Receipt aggregate(final UUID key, final ReceiptLine line, final Receipt receipt) {

        final UUID transaction = line.getObject()
                .getTransaction();

        final Map<String, ReceiptLine> lines = receipt.getLines();
        lines.merge(transaction + "", line, (original, repeated) -> {
            final String message = """
                    Duplicated Receipt line [ {} ] ignored.
                    """;
            logger.warn(message, transaction);
            return original;
        });

        final BigDecimal lineShare = line.getShare();
        final BigDecimal currentDeficit = receipt.getDeficit();
        final BigDecimal aggregateDeficit = currentDeficit.add(lineShare);

        return Receipt.newBuilder()
                .setOrder(key)
                .setLines(lines)
                .setDeficit(aggregateDeficit)
                .setBaggage(new HashMap<>())
                .build();
    }

    public Map<UUID, ReceiptLine> performApportion(final StatementLine object) {

        final List<UUID> orders = baggageSolver.extract(object);

        final BigDecimal amount = object.getAmount();
        final int denominator = orders.size();

        final BigDecimal divisor = BigDecimal.valueOf(denominator);
        final BigDecimal share = amount.divide(divisor, RoundingMode.DOWN);

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
                                    Found StatementLine apportionment [ {} ] of Order [ {} ] to be [ {} ].
                                    """;

                    logger.info(message, object.getTransaction(), order, share);
                })
                .collect(Collectors.toMap(Function.identity(), ignored -> receiptLine));
    }
}
