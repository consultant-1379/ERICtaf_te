package com.ericsson.cifwk.taf.execution.operator.model.jenkins;

import com.google.gson.annotations.Expose;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class ChangeSet {

    @Expose
    private List<Object> items = new ArrayList<Object>();
    @Expose
    private Object kind;

    public List<Object> getItems() {
        return items;
    }

    public void setItems(List<Object> items) {
        this.items = items;
    }

    public Object getKind() {
        return kind;
    }

    public void setKind(Object kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "ChangeSet{" +
                "items=" + items +
                ", kind=" + kind +
                '}';
    }
}
