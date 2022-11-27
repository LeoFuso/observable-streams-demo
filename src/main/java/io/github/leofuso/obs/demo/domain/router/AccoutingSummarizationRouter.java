package io.github.leofuso.obs.demo.domain.router;

import io.github.leofuso.obs.demo.events.StatementLine;

import org.apache.kafka.streams.kstream.Predicate;

import java.util.UUID;

public class AccoutingSummarizationRouter implements StatementLineRouter {

    @Override
    public Predicate<UUID, StatementLine> supports() {
        return StatementLineRouter.super.supports();
    }
}
