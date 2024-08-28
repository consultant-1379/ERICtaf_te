package com.ericsson.cifwk.taf.executor.api;

public class TafTeJenkinsJobImpl implements TafTeJenkinsJob {

    private Type type;
    private String name;
    private String itemName;
    private int number;
    private String url;
    private String fullLogUrl;
    private RunStatus runStatus;
    private Result result;

    public TafTeJenkinsJobImpl() {
    }

    public TafTeJenkinsJobImpl(Type type, String name, String itemName, int number, String url, String fullLogUrl,
                               RunStatus runStatus, Result result) {
        this.type = type;
        this.name = name;
        this.itemName = itemName;
        this.number = number;
        this.url = url;
        this.fullLogUrl = fullLogUrl;
        this.runStatus = runStatus;
        this.result = result;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Result getResult() {
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getScheduleItemName() {
        return itemName;
    }

    @Override
    public RunStatus getRunStatus() {
        return runStatus;
    }

    @Override
    public boolean isComplete() {
        return RunStatus.COMPLETE.equals(runStatus);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getFullLogUrl() {
        return fullLogUrl;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFullLogUrl(String fullLogUrl) {
        this.fullLogUrl = fullLogUrl;
    }

    public void setRunStatus(RunStatus runStatus) {
        this.runStatus = runStatus;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TafTeJenkinsJobImpl that = (TafTeJenkinsJobImpl) o;

        if (number != that.number) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + number;
        return result;
    }

    @Override
    public String toString() {
        return "TafTeJenkinsJobImpl{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", itemName='" + itemName + '\'' +
                ", number=" + number +
                ", url='" + url + '\'' +
                ", fullLogUrl='" + fullLogUrl + '\'' +
                ", runStatus=" + runStatus +
                ", result=" + result +
                '}';
    }
}
