package io.github.leofuso.obs.demo.fixture;

import java.lang.reflect.*;

import org.springframework.context.*;
import org.springframework.test.context.junit.jupiter.*;

import org.apache.avro.specific.*;
import org.junit.jupiter.api.extension.*;

import io.github.leofuso.obs.demo.fixture.annotation.*;

import tech.allegro.schema.json2avro.converter.*;

public class RecordParameterResolver<T extends SpecificRecordBase & SpecificRecord> implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        final Parameter subject = parameter.getParameter();
        final Class<?> parameterType = subject.getType();
        return SpecificRecord.class.isAssignableFrom(parameterType)
                && SpecificRecordBase.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {

        final ApplicationContext context = SpringExtension.getApplicationContext(extensionContext);
        final JsonAvroConverter converter = context.getBean(JsonAvroConverter.class);

        return parameterContext.findAnnotation(RecordParameter.class)
                .map(RecordParameter::value)
                .map(location -> {

                    final Parameter parameter = parameterContext.getParameter();

                    @SuppressWarnings("unchecked")
                    final Class<? extends T> targetClass = (Class<? extends T>) parameter.getType();

                    final TemplatedRecordFactory fixture = TemplatedRecordFactory.getInstance(context, converter);
                    return fixture.make(location, targetClass);
                })
                .orElse(null);
    }
}
