package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Cause {

    @Expose
    private String shortDescription;
    @Expose
    private Object userId;
    @Expose
    private String userName;

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Object getUserId() {
        return userId;
    }

    public void setUserId(Object userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Cause{" +
                "shortDescription='" + shortDescription + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                '}';
    }
}
