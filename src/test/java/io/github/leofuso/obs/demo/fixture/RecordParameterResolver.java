package io.github.leofuso.obs.demo.fixture;

import io.github.leofuso.obs.demo.fixture.annotation.RecordParameter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;

public class RecordParameterResolver<T extends SpecificRecordBase & SpecificRecord> implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        final Parameter subject = parameter.getParameter();
        final Class<?> parameterType = subject.getType();
        return SpecificRecord.class.isAssignableFrom(parameterType)
                && SpecificRecordBase.class.isAssignableFrom(parameterType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return parameterContext.findAnnotation(RecordParameter.class)
                .map(RecordParameter::value)
                .map(location -> {
                    final Parameter parameter = parameterContext.getParameter();
                    return TemplatedRecordFactory.make(location, (Class<? extends T>) parameter.getType());
                })
                .orElse(null);
    }
}
