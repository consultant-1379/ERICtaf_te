package com.ericsson.cifwk.taf.executor.model;

import com.ericsson.cifwk.taf.executor.annotations.Parameter;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 26/01/2016
 */
public class BuildParameterHolderFactory {

    public static <T extends BuildParametersHolder> T createHolder(Class<T> clazz, Function<String, String> paramValueProvider) {
        T result;
        try {
            result = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(String.format("Failed to initialize BuildParameters class %s - " +
                    "perhaps missing a no-arg constructor?", clazz), e);
        } catch (IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
        populateParameterValues(clazz, result, paramValueProvider);
        return result;
    }

    public static <T extends BuildParametersHolder> void populateParameterValues(Class<? super T> clazz, final T instance,
                                                                         Function<String, String> paramValueProvider) {
        walkThroughParameters(clazz, paramValueProvider, new ParamFieldProcessor() {
            @Override
            public void process(Field field, String paramName, Object value) {
                Object convertedToFieldType = ConvertUtils.convert(value, field.getType());
                try {
                    field.set(instance, convertedToFieldType);
                } catch (IllegalAccessException e) {
                    throw Throwables.propagate(e);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends BuildParametersHolder> Map<String, String> asMap(final T instance) {
        final Map<String, String> result = Maps.newTreeMap(new Comparator<String>() {
            @Override
            public int compare(String str1, String str2) {
                return ComparisonChain.start().
                        compare(str1, str2, String.CASE_INSENSITIVE_ORDER).
                        compare(str1, str2).
                        result();
            }
        });
        walkThroughParameters((Class<? super T>) instance.getClass(), new Function<String, String>() {
            @Override
            public String apply(String ignore) {
                return null;
            }
        }, new ParamFieldProcessor() {
            @Override
            public void process(Field field, String paramName, Object ignore) throws IllegalAccessException {
                Object fieldValue = field.get(instance);
                if (fieldValue != null) {
                    result.put(paramName, (String) ConvertUtils.convert(fieldValue, String.class));
                }
            }
        });

        return result;
    }

    private static <T extends BuildParametersHolder> void walkThroughParameters(Class<? super T> clazz,
                                                                                 Function<String, String> paramValueProvider,
                                                                                 ParamFieldProcessor paramFieldProcessor) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Parameter annotation = declaredField.getAnnotation(Parameter.class);
            if (annotation == null) {
                continue;
            }
            String parameterName = annotation.name();
            String parameterValue = paramValueProvider.apply(parameterName);
            String valueToSet = null;
            if (parameterValue != null) {
                valueToSet = parameterValue;
            } else if (StringUtils.isNotBlank(annotation.defaultValue())) {
                valueToSet = annotation.defaultValue();
            }
            declaredField.setAccessible(true);
            try {
                paramFieldProcessor.process(declaredField, parameterName, valueToSet);
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
            }
        }

        // Take care of parent class fields
        Class<? super T> superclass = clazz.getSuperclass();
        if (superclass != null && BuildParametersHolder.class.isAssignableFrom(superclass)) {
            walkThroughParameters(superclass, paramValueProvider, paramFieldProcessor);
        }
    }

    private interface ParamFieldProcessor {
        void process(Field field, String paramName, Object paramValue) throws IllegalAccessException;
    }
}
