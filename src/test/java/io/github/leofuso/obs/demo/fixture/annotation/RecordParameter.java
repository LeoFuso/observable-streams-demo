package io.github.leofuso.obs.demo.fixture.annotation;


import java.lang.annotation.*;

import org.springframework.core.annotation.*;

import org.apache.avro.specific.*;

import io.github.leofuso.obs.demo.fixture.*;


/**
 * An annotation aimed to pass metadata needed to the {@link RecordParameterResolver SpecificRecordParameterResolver} be cabable of
 * instantiating a {@link SpecificRecord SpecificRecord}
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RecordParameter {

    /**
     * Alias for {@link #location()}.
     *
     * @see #location
     */
    @AliasFor("location")
    String value() default "";

    /**
     * JSON-style template <em>location</em> needed to instantiate the {@link SpecificRecord SpecificRecord}.
     *
     * @see #value
     * @see RecordParameterResolver
     */
    @AliasFor("value")
    String location() default "";

}

