package com.ericsson.cifwk.taf.executor.http.client;

public class Authentication {
    private String user;
    private String password;

    public  Authentication(){
        this.user="userldap";
        this.password="UserLdapPassw0rd";
    }

    public String getUser(){
        return this.user;
    }

    public void setUser(String user){
        this.user=user;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password=password;
    }
}
