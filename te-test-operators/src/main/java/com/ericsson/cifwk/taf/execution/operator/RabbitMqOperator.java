package com.ericsson.cifwk.taf.execution.operator;

import com.ericsson.cifwk.taf.data.Host;

/**
 *
 */
public interface RabbitMqOperator {

    boolean exchangeExists(Host mbHost, String exchangeName) throws Exception;

    void deleteExchange(Host mbHost, String exchangeName) throws Exception;

}
