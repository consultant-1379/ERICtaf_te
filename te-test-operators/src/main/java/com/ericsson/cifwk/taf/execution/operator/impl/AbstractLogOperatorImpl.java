package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.execution.operator.LogOperator;
import com.ericsson.cifwk.taf.execution.operator.TeLogVisitor;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 26/04/2016
 */
abstract class AbstractLogOperatorImpl implements LogOperator {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractLogOperatorImpl.class);

    protected final Host teMasterHost;
    protected final String teMasterHostAddress;

    AbstractLogOperatorImpl() {
        this.teMasterHost = DataHandler.getHostByName("te_master");
        this.teMasterHostAddress = teMasterHost.getIp();
        LOGGER.debug("TE Jenkins master host is " + teMasterHostAddress);
    }

    protected void verifyTeBuildLog(String fullLogUrl, TeLogVisitor visitor) {
        List<String> logLines;
        try {
            logLines = Resources.readLines(new URL(fullLogUrl), Charset.defaultCharset());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        String executorLog = Joiner.on("").join(logLines);
        visitor.verifyLog(executorLog);
    }

}
