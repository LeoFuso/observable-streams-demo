package io.github.leofuso.obs.demo.fixture;

import java.lang.reflect.*;

import org.springframework.context.*;
import org.springframework.test.context.junit.jupiter.*;

import org.junit.jupiter.api.extension.*;

import io.github.leofuso.obs.demo.events.*;

import tech.allegro.schema.json2avro.converter.*;

public class StatementLineParameterResolver implements ParameterResolver {

    private static final Class<StatementLine> targetClass = StatementLine.class;

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        final Parameter subject = parameter.getParameter();
        final Class<?> parameterType = subject.getType();
        return targetClass == parameterType;
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
            throws ParameterResolutionException {

        final ApplicationContext context = SpringExtension.getApplicationContext(extensionContext);
        final JsonAvroConverter converter = context.getBean(JsonAvroConverter.class);

        final SpecificRecordFixture fixture = SpecificRecordFixture.fixture(context, converter);
        return fixture.loadRecord("events/statement-line/template.json", targetClass);
    }
}
