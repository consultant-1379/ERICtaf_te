package com.ericsson.cifwk.taf.executor.api;

import java.io.Serializable;

public class Host implements Serializable {

    private String hostName;
    private String ipAddress;
    private Integer sshPort;
    private Credentials credentials;

    public Host() {
    }

    public Host(final String hostName, final String ipAddress, final Integer sshPort, final String username, final String password) {
        this.hostName = hostName;
        this.ipAddress = ipAddress;
        this.sshPort = sshPort;
        credentials = new Credentials(username, password);
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
    
    public String getHostName() {
        return hostName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public Credentials getCredentials() {
        return credentials;
    }
    
    public String getUserName() {
        return credentials.getUserName();
    }

    public String getPassword() {
        return credentials.getPassword();
    }
}
