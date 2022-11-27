package io.github.leofuso.obs.demo.domain.router;

import java.util.UUID;

import org.apache.kafka.streams.kstream.Predicate;

import io.github.leofuso.obs.demo.events.StatementLine;

public class AccoutingSummarizationRouter implements StatementLineRouter {

    @Override
    public Predicate<UUID, StatementLine> supports() {
        return StatementLineRouter.super.supports();
    }
}
