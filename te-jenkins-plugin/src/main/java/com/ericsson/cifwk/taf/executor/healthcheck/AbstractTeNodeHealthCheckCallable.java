package com.ericsson.cifwk.taf.executor.healthcheck;

import net.sf.json.JSONObject;
import org.jenkinsci.remoting.RoleChecker;

public abstract class AbstractTeNodeHealthCheckCallable implements RemoteHealthCheckCallable {

    protected final String nodeName;

    public AbstractTeNodeHealthCheckCallable(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public final String call() throws Exception {
        HealthParam check = new HealthParam(getCheckName(nodeName), nodeName);
        return doCheck(check);
    }

    protected String failCheck(HealthParam check, String message) {
        check.setPassed(false).setDescription(message);
        return toJson(check);
    }

    protected String toJson(HealthParam check) {
        return JSONObject.fromObject(check).toString();
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
    }
}
