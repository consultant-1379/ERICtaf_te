package com.ericsson.cifwk.taf.execution.operator.impl;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.execution.operator.EventRepositoryOperator;
import com.ericsson.cifwk.taf.execution.operator.model.er.EventSearchResult;
import com.ericsson.cifwk.taf.executor.api.ScheduleRequest;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.duraci.datawrappers.BaselinePart;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.eiffelmessage.deserialization.Deserializer;
import com.ericsson.duraci.eiffelmessage.deserialization.exceptions.MessageDeserializationException;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelBaselineDefinedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobStartedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobStepFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelJobStepStartedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteStartedEvent;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@Operator
public class EventRepositoryOperatorImpl implements EventRepositoryOperator {
    private static final String ER_REST_API_EVENTS_URI = "/eventrepository/restapi/events";
    private static final Logger LOGGER = LoggerFactory.getLogger(EventRepositoryOperatorImpl.class);

    private final Deserializer eiffelMessageDeserializer = new Deserializer(new JavaLoggerEiffelLog(this.getClass()));
    private final HttpTool httpTool = HttpToolBuilder.newBuilder(getErRestServiceBase()).followRedirect(true).build();

    @Override
    public EiffelMessage findJobStartedEventMessage(ExecutionId executionId) {
        return findEventMessage(executionId, EiffelJobStartedEvent.class);
    }

    @Override
    public EiffelMessage findJobFinishedEventMessage(ExecutionId executionId) {
        return findEventMessage(executionId, EiffelJobFinishedEvent.class);
    }

    @Override
    public EiffelMessage findEventMessage(ExecutionId executionId, Class<? extends EiffelEvent> eventClass) {
        return returnFirstMessageOrNull(findEventMessages(executionId, eventClass));
    }

    @Override
    public EiffelMessage findEventMessage(EventId eventId, Class<? extends EiffelEvent> eventClass) {
        EiffelMessage[] eiffelMessagesFromUrl = getEiffelMessagesFromUrl(String.format("%s?eventId=%s&eventType=%s",
                ER_REST_API_EVENTS_URI, eventId.toString(), eventClass.getSimpleName()));
        return returnFirstMessageOrNull(eiffelMessagesFromUrl);
    }

    @Override
    public List<EiffelMessage> findEventMessages(ExecutionId executionId, Class<? extends EiffelEvent> eventClass) {
        EiffelMessage[] eiffelMessagesFromUrl = getEiffelMessagesFromUrl(String.format("%s?eventData.jobExecutionId=%s&eventType=%s",
                        ER_REST_API_EVENTS_URI, executionId.toString(), eventClass.getSimpleName()));
        return Arrays.asList(eiffelMessagesFromUrl);
    }

    @Override
    public List<EiffelMessage> findTestSuiteStartedEventMessages(ExecutionId parentExecutionId) {
        EiffelMessage[] eiffelMessagesFromUrl = getEiffelMessagesFromUrl(String.format("%s?eventData.parentExecutionId=%s&eventType=%s",
                ER_REST_API_EVENTS_URI, parentExecutionId.toString(), EiffelTestSuiteStartedEvent.class.getSimpleName()));
        return Arrays.asList(eiffelMessagesFromUrl);
    }

    @Override
    public EiffelMessage findTestSuiteFinishedEventMessage(ExecutionId testSuiteExecutionId) {
        EiffelMessage[] eiffelMessagesFromUrl = getEiffelMessagesFromUrl(String.format("%s?eventData.testSuiteExecutionId=%s&eventType=%s",
                ER_REST_API_EVENTS_URI, testSuiteExecutionId.toString(), EiffelTestSuiteFinishedEvent.class.getSimpleName()));
        return returnFirstMessageOrNull(eiffelMessagesFromUrl);
    }

    @Override
    public List<EiffelMessage> findJobStepStartedEventMessages(ExecutionId jobExecutionId) {
        EiffelMessage[] eiffelMessagesFromUrl = getEiffelMessagesFromUrl(String.format("%s?eventData.parentExecutionId=%s&eventType=%s",
                ER_REST_API_EVENTS_URI, jobExecutionId.toString(), EiffelJobStepStartedEvent.class.getSimpleName()));
        return Arrays.asList(eiffelMessagesFromUrl);
    }

    @Override
    public EiffelMessage findJobStepFinishedEventMessage(ExecutionId jobStepExecutionId) {
        EiffelMessage[] eiffelMessagesFromUrl = getEiffelMessagesFromUrl(String.format("%s?eventData.jobStepExecutionId=%s&eventType=%s",
                ER_REST_API_EVENTS_URI, jobStepExecutionId.toString(), EiffelJobStepFinishedEvent.class.getSimpleName()));
        return returnFirstMessageOrNull(eiffelMessagesFromUrl);
    }

    private EiffelMessage returnFirstMessageOrNull(EiffelMessage[] eiffelMessages) {
        return returnFirstMessageOrNull(Arrays.asList(eiffelMessages));
    }

    private EiffelMessage returnFirstMessageOrNull(List<EiffelMessage> eiffelMessages) {
        return (eiffelMessages == null || eiffelMessages.isEmpty()) ? null : eiffelMessages.get(0);
    }

    @Override
    public List<EiffelMessage> getEventDownstream(EventId eventId) {
        EiffelMessage[] eiffelMessagesFromUrl =
                getEiffelMessagesFromUrl(String.format("%s/%s/downstream/", ER_REST_API_EVENTS_URI, eventId.toString()));
        return Arrays.asList(eiffelMessagesFromUrl);
    }

    @Override
    public ScheduleRequest getSchedule(EiffelBaselineDefinedEvent triggerEvent) {
        Collection<BaselinePart> baselineParts = triggerEvent.getConsistsOf();
        List<BaselinePart> schedules = getSchedules(baselineParts);
        assertThat(schedules, Matchers.hasSize(1));
        return getScheduleFromTag(schedules.get(0).getTag());
    }

    public EiffelMessage[] getEiffelMessagesFromUrl(String url) {
        HttpResponse httpResponse = httpTool.get(url);
        String searchResultJson = httpResponse.getBody();
        LOGGER.trace("Events for Uri: {}\n{} ", url, searchResultJson);

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        EventSearchResult searchResults = gson.fromJson(searchResultJson, EventSearchResult.class);
        List<Object> eventMessages = searchResults.getItems();
        if (CollectionUtils.isNotEmpty(eventMessages)) {
            EiffelMessage[] result = new EiffelMessage[eventMessages.size()];
            try {
                for (int i = 0; i < eventMessages.size(); i++) {
                    String eventMessageJson = gson.toJson(eventMessages.get(i));
                    result[i] = eiffelMessageDeserializer.deserialize(eventMessageJson);
                }
                return result;
            } catch (MessageDeserializationException e) {
                throw Throwables.propagate(e);
            }
        }
        return new EiffelMessage[0];
    }

    private Host getErRestServiceBase() {
        return DataHandler.getHostByName("event_repository");
    }

    private List<BaselinePart> getSchedules(Collection<BaselinePart> baselineParts) {
        return extractBaselineParts(baselineParts, "Schedule:");
    }

    private List<BaselinePart> extractBaselineParts(Collection<BaselinePart> baselineParts, final String tagName) {
        Iterable<BaselinePart> filtered = Iterables.filter(baselineParts, new Predicate<BaselinePart>() {
            @Override
            public boolean apply(BaselinePart baselinePart) {
                String baselinePartTag = baselinePart.getTag();
                return baselinePartTag != null && baselinePartTag.contains(tagName);
            }
        });
        assertNotNull(filtered);
        return Lists.newArrayList(filtered);
    }

    private ScheduleRequest getScheduleFromTag(String tag) {
        Gson gson = new Gson();
        return gson.fromJson(tag.replace("Schedule:", ""), ScheduleRequest.class);
    }

}
