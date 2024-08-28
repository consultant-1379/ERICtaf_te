
package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class JobReference {

    @Expose
    private String name;
    @Expose
    private String url;
    @Expose
    private String color;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "JobReference{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

}
