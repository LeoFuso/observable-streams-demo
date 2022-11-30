package io.github.leofuso.obs.demo.domain.branch;

import java.util.*;
import java.util.function.*;

import org.apache.kafka.common.utils.*;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.*;

import io.github.leofuso.obs.demo.events.*;

import com.google.common.base.*;

public interface StatementLineBranch {

    Logger logger = LoggerFactory.getLogger(StatementLineBranch.class);

    static <S extends StatementLineBranch> S produce(Class<S> branchClass) {
        return Utils.newInstance(branchClass);
    }

    static String namedRouter(StatementLineBranch branch) {
        final Class<? extends StatementLineBranch> branchClass = branch.getClass();
        final String className = branchClass.getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, className);
    }

    private KStream<UUID, StatementLine> trace(KStream<UUID, StatementLine> kStream) {
        final boolean shouldTrace = logger.isTraceEnabled();
        if (!shouldTrace) {
            return kStream;
        }

        final Named namedTrace = Named.as(name() + "-trace");
        return kStream.peek((key, value) -> {
            final String message =
                    """
                            Branching StatementLine [{}] to [{}]""";
            logger.trace(message, key, topic());
        }, namedTrace);
    }

    default Predicate<UUID, StatementLine> supports() {
        return (key, value) -> false;
    }

    default Branched<UUID, StatementLine> branched() {
        final String topic = topic();
        final Produced<UUID, StatementLine> produced = Produced.as(name() + "-produced");
        final Consumer<KStream<UUID, StatementLine>> kStreams = kStream -> trace(kStream).to(topic, produced);

        final String branchedName = "-" + namedRouter(this);
        return Branched.withConsumer(kStreams, branchedName);
    }

    default String topic() {
        return "obs." + name();
    }

    default String name() {
        return namedRouter(this);
    }

}
