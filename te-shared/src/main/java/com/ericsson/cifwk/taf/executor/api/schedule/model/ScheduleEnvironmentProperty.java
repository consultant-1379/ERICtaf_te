package com.ericsson.cifwk.taf.executor.api.schedule.model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 12/02/2016
 */
public final class ScheduleEnvironmentProperty {

    private String type;
    private String key;
    private String value;

    public ScheduleEnvironmentProperty() {
    }

    public ScheduleEnvironmentProperty(String type, String key, String value) {
        setType(type);
        setKey(key);
        setValue(value);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        Preconditions.checkArgument(StringUtils.isNotBlank(type), "Property type cannot be empty");
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "Property name cannot be null");
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduleEnvironmentProperty that = (ScheduleEnvironmentProperty) o;

        if (!key.equals(that.key)) return false;
        if (!type.equals(that.type)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ScheduleEnvironmentProperty{" +
                "type='" + type + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
