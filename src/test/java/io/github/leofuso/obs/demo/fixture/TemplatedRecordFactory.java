package io.github.leofuso.obs.demo.fixture;


import jakarta.annotation.Nullable;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Objects;

/**
 * Static Factory for {@link SpecificRecord SpecificRecords} located within the classpath that can be initialized with default parameters
 * also found within the current classpath.
 */
public class TemplatedRecordFactory {

    private static final Logger logger = LoggerFactory.getLogger(TemplatedRecordFactory.class);
    private static final JsonAvroConverter converter = new JsonAvroConverter();

    private TemplatedRecordFactory() { /* Noop */ }

    public static <T extends SpecificRecordBase & SpecificRecord> T make(final String location, final Class<T> tClass) {
        try {
            Objects.requireNonNull(location, "Location is required.");
            Objects.requireNonNull(tClass, "Class is required.");
            return doMake(location, tClass);
        } catch (RuntimeException ex) {
            logger.error("Unexpected exception when making SpecificRecord: ", ex);
            return null;
        }
    }

    private static <T extends SpecificRecordBase & SpecificRecord> T doMake(final String location, final Class<T> tClass) {
        try {
            final Field schemaField = tClass.getDeclaredField("SCHEMA$");
            schemaField.setAccessible(true);
            final Schema schema = (Schema) schemaField.get(null);
            return doLoadSpecializedRecord(location, tClass, schema);
        } catch (NoSuchFieldException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private static <T extends SpecificRecordBase & SpecificRecord> T doLoadSpecializedRecord(
            final String location,
            final Class<T> tClass,
            final Schema schema
    ) throws IOException {

        final URL resource = TemplatedRecordFactory.class
                .getClassLoader()
                .getResource(location);

        if (resource == null) {
            return null;
        }

        try (final InputStream inputStream = resource.openStream()) {
            final byte[] stream = inputStream.readAllBytes();
            return converter.convertToSpecificRecord(stream, tClass, schema);
        }

    }
}
