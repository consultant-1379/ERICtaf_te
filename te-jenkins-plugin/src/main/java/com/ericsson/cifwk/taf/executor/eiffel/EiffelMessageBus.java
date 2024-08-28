package com.ericsson.cifwk.taf.executor.eiffel;

import com.ericsson.cifwk.taf.executor.TestResult;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.mmparser.clitool.EiffelConfig;
import com.ericsson.duraci.eiffelmessage.sending.MessageSender;
import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;
import hudson.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;

public class EiffelMessageBus {

    public static final Logger LOGGER = LoggerFactory.getLogger(EiffelMessageBus.class);

    protected MessageSender sender;

    private final Deque<EiffelParent> parents = new LinkedList<>();
    private final String domainId;

    public EiffelMessageBus(String domainId) {
        this.domainId = domainId;
    }

    public void connect(String hostName, String exchange) {
        EiffelConfig config = new EiffelConfig(domainId, exchange, hostName);
        MessageSender.Factory factory = new MessageSender.Factory(config);
        sender = factory.create();
    }

    public EiffelParent getSentParent() {
        return parents.peek();
    }

    public void pushAsParent(EventId eventId, ExecutionId eventExecutionId) {
        parents.push(new EiffelParent(eventId, eventExecutionId));
    }

    public synchronized EventId sendStart(EiffelEvent event, ExecutionId eventExecutionId, EventId... parentEventIds) {
        EiffelParent parent = parents.peek();
        EiffelMessage message;
        if (parent != null) {
            parentEventIds = ObjectArrays.concat(parent.getEventId(), parentEventIds);
            message = toMessage(event, parentEventIds);
        } else {
            message = toMessage(event, parentEventIds);
        }
        EventId eventId = message.getEventId();
        pushAsParent(eventId, eventExecutionId);
        try {
            LOGGER.debug("Sending message: {}", message);
            sender.send(message);
        } catch (Exception e) {
            LOGGER.warn("Failed to send event " + eventId + " ", e);
        }
        return eventId;
    }

    public synchronized void sendFinish(EiffelEvent event) {
        EiffelParent parent = parents.pop();
        Preconditions.checkNotNull(parent, "Could not find start event for this finish event");
        EiffelMessage message = toMessage(event, parent.getEventId());
        try {
            LOGGER.debug("Sending message: {}", message);
            sender.send(message);
        } catch (Exception e) {
            LOGGER.warn("Failed to send event " + message.getEventId(), e);
        }
    }

    public EiffelMessage toMessage(EiffelEvent event, EventId... parentEventIds) {
        return EiffelMessage.Factory.configure(domainId, event)
                .addInputEventIds(parentEventIds)
                .create();
    }

    public static ResultCode buildResultCode(Result buildResult) {
        if (buildResult == null) {
            return ResultCode.UNRECOGNIZED;
        } else if (equalResult(buildResult, Result.SUCCESS)) {
            return ResultCode.SUCCESS;
        } else if (equalResult(buildResult, Result.UNSTABLE)) {
            return ResultCode.UNSTABLE;
        } else if (equalResult(buildResult, Result.FAILURE)) {
            return ResultCode.FAILURE;
        } else if (equalResult(buildResult, Result.NOT_BUILT)) {
            return ResultCode.NOT_BUILT;
        } else if (equalResult(buildResult, Result.ABORTED)) {
            return ResultCode.ABORTED;
        } else {
            return ResultCode.UNRECOGNIZED;
        }
    }

    public static ResultCode executorResultCode(TestResult.Status testResultStatus) {
        switch (testResultStatus) {
            case SUCCESS:
                return ResultCode.SUCCESS;
            case FAILURE:
            case ERROR:
                return ResultCode.FAILURE;
            default:
                return ResultCode.UNRECOGNIZED;
        }
    }

    private static boolean equalResult(Result first, Result second) {
        String firstName = first.toExportedObject();
        String secondName = second.toExportedObject();
        return firstName.equals(secondName);
    }

    public void disconnect() {
        try {
            if (sender != null) sender.dispose();
        } catch (Exception ignore) {
            LOGGER.warn("IGNORE :" + ignore.getMessage(), ignore);
        }
    }

}
