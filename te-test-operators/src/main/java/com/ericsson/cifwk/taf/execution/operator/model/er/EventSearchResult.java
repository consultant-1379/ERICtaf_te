package com.ericsson.cifwk.taf.execution.operator.model.er;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class EventSearchResult {
    @Expose
    private Integer pageNo;

    @Expose
    private Integer pageSize;

    @Expose
    private Integer totalNumberItems;

    @Expose
    private List<Object> items = new ArrayList<>();

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalNumberItems() {
        return totalNumberItems;
    }

    public void setTotalNumberItems(Integer totalNumberItems) {
        this.totalNumberItems = totalNumberItems;
    }

    public List<Object> getItems() {
        return items;
    }

    public void setItems(List<Object> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "EventSearchResult{" +
                "pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                ", totalNumberItems=" + totalNumberItems +
                ", items=" + items +
                '}';
    }
}
