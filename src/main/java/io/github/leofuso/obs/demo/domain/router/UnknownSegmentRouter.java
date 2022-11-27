package io.github.leofuso.obs.demo.domain.router;

import io.github.leofuso.obs.demo.events.*;

import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Consumer;

public class UnknownSegmentRouter implements StatementLineRouter {

    private static final Logger logger = LoggerFactory.getLogger(UnknownSegmentRouter.class);

    private static final AccoutingSummarizationRouter DEFAULT_ROUTER =
            StatementLineRouter.produce(AccoutingSummarizationRouter.class);

    @Override
    public Predicate<UUID, StatementLine> supports() {
        return (key, value) -> {
            final Details details = value.getDetails();
            final Segment segment = details.getSegment();
            return Segment.UNKNOWN == segment;
        };
    }

    @Override
    public Branched<UUID, StatementLine> branched() {

        final String skipping = name() + "-peeker";
        final String processorName = name() + "-producer";
        final Produced<UUID, StatementLine> produced = Produced.as(processorName);

        final Consumer<KStream<UUID, StatementLine>> kStreamConsumer =
                kStream -> kStream.peek(
                        (key, value) -> {
                            final String message =
                                    """
                                            Skipping apportionment [ {} ]: Unknown route.
                                            """;
                            logger.warn(message, key);
                        },
                        Named.as(skipping)
                ).to(topic(), produced);

        final String branchedName = "-" + StatementLineRouter.namedRouter(this) + "-branch";
        return Branched.withConsumer(kStreamConsumer, branchedName);
    }

    @Override
    public String topic() {
        return DEFAULT_ROUTER.topic();
    }
}
