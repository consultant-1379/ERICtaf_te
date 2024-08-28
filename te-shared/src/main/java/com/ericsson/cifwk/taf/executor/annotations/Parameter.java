package com.ericsson.cifwk.taf.executor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for build parameters defined in instances of {@link com.ericsson.cifwk.taf.executor.model.BuildParametersHolder}.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/01/2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Parameter {

    /**
     * @return name of the parameter
     */
    String name();

    /**
     * @return default value to apply if value is not found
     */
    String defaultValue() default "";

}
