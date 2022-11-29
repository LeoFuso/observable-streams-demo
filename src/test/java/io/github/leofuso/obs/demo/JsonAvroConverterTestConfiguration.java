package io.github.leofuso.obs.demo;

import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;

import tech.allegro.schema.json2avro.converter.*;

@TestConfiguration
public class JsonAvroConverterTestConfiguration {

    @Bean
    public JsonAvroConverter jsonAvroConverter() {
        return new JsonAvroConverter();
    }

}
