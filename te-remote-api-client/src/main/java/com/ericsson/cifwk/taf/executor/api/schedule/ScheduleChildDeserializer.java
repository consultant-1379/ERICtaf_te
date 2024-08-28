package com.ericsson.cifwk.taf.executor.api.schedule;

import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleChild;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItem;
import com.ericsson.cifwk.taf.executor.api.schedule.model.ScheduleItemGroup;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ScheduleChildDeserializer implements JsonDeserializer<ScheduleChild> {
    @Override
    public ScheduleChild deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.get("parallel") != null) {
            return context.deserialize(jsonObject, ScheduleItemGroup.class);
        }
        else {
            return context.deserialize(jsonObject, ScheduleItem.class);
        }
    }
}
