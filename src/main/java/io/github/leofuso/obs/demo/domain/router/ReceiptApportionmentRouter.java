package io.github.leofuso.obs.demo.domain.router;

import java.util.Set;
import java.util.UUID;

import org.apache.kafka.streams.kstream.Predicate;

import io.github.leofuso.obs.demo.events.*;

public class ReceiptApportionmentRouter implements StatementLineRouter {

    private static final Set<Segment> SUPPORTED = Set.of(
            Segment.ROUTE,
            Segment.INCENTIVE,
            Segment.SHIFT,
            Segment.MVP
    );

    @Override
    public Predicate<UUID, StatementLine> supports() {
        return (key, value) -> {
            final Details details = value.getDetails();
            final Segment segment = details.getSegment();
            return SUPPORTED.contains(segment);
        };
    }
}
