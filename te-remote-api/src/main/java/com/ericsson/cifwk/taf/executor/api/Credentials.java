package com.ericsson.cifwk.taf.executor.api;


import java.io.Serializable;

public class Credentials implements Serializable {

    private String username;
    private String password;
    
    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUserName() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setUserName(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credentials that = (Credentials) o;

        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "username='" + username + '\'' +
                ", password=<HIDDEN>" + // NOSONAR
                '}';
    }
}
