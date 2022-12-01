package io.github.leofuso.obs.demo.fixture;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.springframework.core.io.*;

import org.apache.avro.*;
import org.apache.avro.specific.*;

import tech.allegro.schema.json2avro.converter.*;

public class TemplatedRecordFactory {

    private final ResourceLoader loader;
    private final JsonAvroConverter converter;

    private TemplatedRecordFactory(ResourceLoader loader, JsonAvroConverter converter) {
        this.loader = loader;
        this.converter = converter;
    }

    public static TemplatedRecordFactory getInstance(ResourceLoader loader, JsonAvroConverter converter) {
        Objects.requireNonNull(loader, "ResourceLoader is required.");
        Objects.requireNonNull(converter, "JsonAvroConverter is required.");
        return new TemplatedRecordFactory(loader, converter);
    }

    public <T extends SpecificRecordBase & SpecificRecord> T make(final String location, final Class<T> tClass) {
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

        final byte[] stream = loader
                .getResource(location)
                .getInputStream()
                .readAllBytes();

        return converter.convertToSpecificRecord(stream, tClass, schema);
    }
}
