package io.github.leofuso.obs.demo.core;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.kafka.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.*;
import org.springframework.core.io.*;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;
import org.springframework.test.context.*;

import org.apache.avro.*;
import org.apache.avro.specific.*;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.*;

import io.confluent.kafka.streams.serdes.avro.*;
import io.github.leofuso.obs.demo.*;

import tech.allegro.schema.json2avro.converter.*;

@SpringBootTest(
        properties =
                {
                        "spring.main.banner-mode=off",

                        "spring.kafka.properties.schema.registry.url=mock://obs.demo",
                        "spring.kafka.properties.auto.register.schemas=true",
                        "spring.kafka.properties.value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy",

                        "spring.kafka.streams.auto-startup=false",
                        "spring.kafka.streams.application-id=obs.managment",
                        "spring.kafka.streams.properties.default.key.serde=org.apache.kafka.common.serialization.Serdes$UUIDSerde",
                        "spring.kafka.streams.properties.default.value.serde=io.confluent.kafka.streams.serdes.avro.ReflectionAvroSerde"
                }
        ,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@Import(JsonAvroConverterTestConfiguration.class)
public abstract class CoreTest {

    protected TopologyTestDriver testDriver;

    protected TopicFixture topicFixture;

    @Autowired
    private StreamsBuilder streamsBuilder;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private JsonAvroConverter jsonAvroConverter;

    @MockBean
    @SuppressWarnings("unused")
    private KafkaAdmin kafkaAdmin;

    protected abstract void contextSetup();

    @BeforeEach
    protected void setUp() {

        final Map<String, Object> properties = kafkaProperties.buildStreamsProperties();
        final KafkaStreamsConfiguration streamsConfig = new KafkaStreamsConfiguration(properties);
        final Properties streamProperties = streamsConfig.asProperties();

        final Topology topology = streamsBuilder.build(streamProperties);
        testDriver = new TopologyTestDriver(topology, streamProperties);
        topicFixture = new TopicFixture();

        contextSetup();
    }

    @AfterEach
    void afterAll() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    protected <T extends SpecificRecordBase & SpecificRecord> T loadRecord(final String location, final Class<T> tClass) {
        try {
            final Field schemaField = tClass.getDeclaredField("SCHEMA$");
            schemaField.setAccessible(true);
            final Schema schema = (Schema) schemaField.get(null);
            return doLoadSpecializedRecord(location, tClass, schema);
        } catch (NoSuchFieldException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends SpecificRecordBase & SpecificRecord> T doLoadSpecializedRecord(
            final String location,
            final Class<T> tClass,
            final Schema schema
    )
            throws IOException {

        final byte[] stream = resourceLoader
                .getResource(location)
                .getInputStream()
                .readAllBytes();

        return jsonAvroConverter.convertToSpecificRecord(stream, tClass, schema);
    }

    public class TopicFixture {

        public <T extends SpecificRecord> TestInputTopic<UUID, T> input(final String name) {

            final UUIDSerializer keySerializer = new UUIDSerializer();

            final ReflectionAvroSerializer<T> valueSerializer = new ReflectionAvroSerializer<>();
            final Map<String, Object> config = kafkaProperties.buildStreamsProperties();
            valueSerializer.configure(config, false);

            return testDriver.createInputTopic(name, keySerializer, valueSerializer);
        }

        public <T extends SpecificRecord> TestOutputTopic<UUID, T> output(final String topicName) {

            final UUIDDeserializer keyDeserializer = new UUIDDeserializer();

            final ReflectionAvroDeserializer<T> valueDeserializer = new ReflectionAvroDeserializer<>();
            final Map<String, Object> config = kafkaProperties.buildStreamsProperties();
            valueDeserializer.configure(config, false);

            return testDriver.createOutputTopic(topicName, keyDeserializer, valueDeserializer);
        }

    }
}
