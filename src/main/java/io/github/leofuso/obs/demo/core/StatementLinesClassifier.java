package io.github.leofuso.obs.demo.core;

import io.github.leofuso.obs.demo.core.configuration.TopicFixture;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class StatementLinesClassifier {

    private static final Logger logger = LoggerFactory.getLogger(StatementLinesClassifier.class);

    private final StreamsBuilder streamsBuilder;

    public StatementLinesClassifier(final StreamsBuilder streamsBuilder) {
        this.streamsBuilder = Objects.requireNonNull(streamsBuilder, "StreamsBuilder [streamsBuilder] is required.");
    }

    @Autowired
    public void classifier() {

        final String source = TopicFixture.APPROVED_STATEMENT_LINE;
        streamsBuilder
                .stream(source, Consumed.as("statement-line-source"))
                .filter((key, value) -> Objects.nonNull(key) || Objects.nonNull(value), Named.as("statement-null-filter"))
                .split(Named.as("statement-splitter"))
                //.branch()
                .defaultBranch();
    }
}
