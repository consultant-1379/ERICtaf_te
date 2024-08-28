
package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ViewReference {

    @Expose
    private String name;
    @Expose
    private String url;

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

    @Override
    public String toString() {
        return "ViewReference{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

}
