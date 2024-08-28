
package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class HealthReport {

    @Expose
    private String description;
    @Expose
    private String iconUrl;
    @Expose
    private Integer score;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "HealthReport{" +
                "description='" + description + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", score=" + score +
                '}';
    }

}
