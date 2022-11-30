package io.github.leofuso.obs.demo.fixture;

import java.lang.reflect.*;
import java.util.*;

import org.junit.jupiter.api.extension.*;

public class UUIDParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        final Parameter subject = parameter.getParameter();
        final Class<?> parameterType = subject.getType();
        return UUID.class == parameterType;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return UUID.randomUUID();
    }
}
