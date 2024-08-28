package com.ericsson.cifwk.taf.executor.node;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleEnvironmentProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 18/02/2016
 */
public class ScheduleEnvironmentPropertyProvider {

    private final List<ScheduleEnvironmentProperty> environmentProperties;
    private final PrintStream buildLog;
    private final Map<String, Set<String>> excludedSettings = Maps.newHashMap();

    public ScheduleEnvironmentPropertyProvider(PrintStream buildLog, String environmentPropertyJson) {
        this.buildLog = buildLog;
        Gson gson = new Gson();
        List<ScheduleEnvironmentProperty> parsedProperties =
                gson.fromJson(environmentPropertyJson, new TypeToken<List<ScheduleEnvironmentProperty>>() {}.getType());
        this.environmentProperties = (parsedProperties != null) ? parsedProperties : new ArrayList<ScheduleEnvironmentProperty>();
    }

    public Integer getRequiredJavaVersion() {
        return findProperty(Property.Type.JVM, "version", Integer.class);
    }

    public String getJavaOpts() {
        return findProperty(Property.Type.JVM, "options");
    }

    public Integer getMaxThreads() {
        return findProperty(Property.Type.JVM, "maxThreads", Integer.class);
    }

    public Map<String,String> getAllSystemProperties() {
        return getAllPropertiesOfType(Property.Type.SYSTEM);
    }

    @VisibleForTesting
    Map<String,String> getAllPropertiesOfType(final String type) {
        Map<String,String> result = Maps.newLinkedHashMap();
        for (ScheduleEnvironmentProperty property : environmentProperties) {
            String propertyName = property.getKey();
            if (StringUtils.equals(property.getType(), type)) {
                if (isExcluded(type, propertyName)) {
                    buildLog.println(String.format("WARN: illegal system setting '%s' will be ignored in the test run", propertyName));
                } else {
                    result.put(propertyName, property.getValue());
                }
            }
        }
        return result;
    }

    private boolean isExcluded(String type, String key) {
        Set<String> excludedOnes = excludedSettings.get(type);
        return excludedOnes != null && excludedOnes.contains(key);
    }

    @VisibleForTesting
    String findProperty(String type, final String key) {
        return findProperty(type, key, String.class);
    }

    @VisibleForTesting
    @SuppressWarnings("unchecked")
    <T> T findProperty(final String type, final String key, Class<T> clazz) {
        Optional<ScheduleEnvironmentProperty> property = Iterables.tryFind(environmentProperties, new Predicate<ScheduleEnvironmentProperty>() {
            @Override
            public boolean apply(ScheduleEnvironmentProperty input) {
                return StringUtils.equals(input.getType(), type)
                        && StringUtils.equals(input.getKey(), key)
                        && !isExcluded(type, key);
            }
        });
        if (!property.isPresent()) {
            return null;
        }
        return (T) ConvertUtils.convert(property.get().getValue(), clazz);
    }

    public void setExcludedSystemSettings(Set<String> excludedSystemSettings) {
        setExcludedSettings(Property.Type.SYSTEM, excludedSystemSettings);
    }

    @VisibleForTesting
    void setExcludedSettings(String type, Set<String> excludedSettingNames) {
        if (!excludedSettings.containsKey(type)) {
            excludedSettings.put(type, Sets.<String>newHashSet());
        }
        Set<String> existingExclusions = excludedSettings.get(type);
        existingExclusions.addAll(excludedSettingNames);
    }

    public interface Property {
        public interface Type {
            static String JVM = "jvm";
            static String SYSTEM = "system";
        }
    }

}
