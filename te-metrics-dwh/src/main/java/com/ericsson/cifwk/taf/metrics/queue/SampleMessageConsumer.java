package com.ericsson.cifwk.taf.metrics.queue;

import com.ericsson.cifwk.taf.performance.sample.BodyConsumer;
import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.ericsson.cifwk.taf.performance.sample.impl.AmqpClient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SampleMessageConsumer {

    private final Logger logger = LoggerFactory.getLogger(SampleMessageConsumer.class);

    protected final Kryo kryo;
    protected final AmqpClient amqp;
    protected final DbWriter dbWriter;

    public SampleMessageConsumer(Kryo kryo,
                                 AmqpClient amqp,
                                 DbWriter dbWriter) {
        this.kryo = kryo;
        this.amqp = amqp;
        this.dbWriter = dbWriter;

    }

    public void start() throws IOException {
        amqp.connect();
        amqp.subscribe(new BodyConsumer() {
            @Override
            public void handle(byte[] body) {
                consumeSample(body);
            }
        });
    }

    protected void consumeSample(byte[] body) {
        try {
            Input input = new Input(body);
            Sample sample = kryo.readObject(input, Sample.class);
            dbWriter.write(sample);
        } catch (Exception e) {
            logger.error("Error consuming sample from amqp", e);
        }
    }

    public void shutdown() {
        try {
            amqp.shutdown();
            dbWriter.close();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
