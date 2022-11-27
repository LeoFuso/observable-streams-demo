package io.github.leofuso.obs.demo.domain.router;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;

import io.github.leofuso.obs.demo.events.StatementLine;

public interface StatementLineRouter {

    Logger logger = LoggerFactory.getLogger(StatementLineRouter.class);

    static <S extends StatementLineRouter> S produce(Class<S> routerClass) {
        return Utils.newInstance(routerClass);
    }

    static String namedRouter(StatementLineRouter router) {
        final Class<? extends StatementLineRouter> routerClass = router.getClass();
        final String className = routerClass.getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, className);
    }

    private KStream<UUID, StatementLine> trace(KStream<UUID, StatementLine> kStream) {
        final boolean shouldTrace = logger.isTraceEnabled();
        if (!shouldTrace) {
            return kStream;
        }

        final String peeker = name() + "-peeker";
        return kStream.peek((key, value) -> {
            final String message =
                    """
                            Routing StatementLine [ {} ].
                            """;
            logger.trace(message, key);
        }, Named.as(peeker));
    }

    default Predicate<UUID, StatementLine> supports() {
        return (key, value) -> false;
    }

    default Branched<UUID, StatementLine> branched() {
        final String topic = topic();

        final String processorName = name() + "-producer";
        final Produced<UUID, StatementLine> produced = Produced.as(processorName);
        final Consumer<KStream<UUID, StatementLine>> kStreamConsumer = kStream -> trace(kStream).to(topic, produced);

        final String branchedName = "-" + namedRouter(this) + "-branch";
        return Branched.withConsumer(kStreamConsumer, branchedName);
    }

    default String topic() {
        return "obs.internal-" + name();
    }

    default String name() {
        return namedRouter(this);
    }

}
