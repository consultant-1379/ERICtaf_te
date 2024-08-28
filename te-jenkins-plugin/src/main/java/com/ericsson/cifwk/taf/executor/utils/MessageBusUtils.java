package com.ericsson.cifwk.taf.executor.utils;

import com.ericsson.cifwk.taf.executor.eiffel.EiffelMessageBus;
import com.ericsson.cifwk.taf.executor.model.GlobalTeSettings;

public class MessageBusUtils {

    private static EiffelMessageBus genericBus;

    public static EiffelMessageBus initializeAndConnect(GlobalTeSettings globalTeSettings) {
        return initializeAndConnect(
                globalTeSettings.getReportMbDomainId(),
                globalTeSettings.getMbHostWithPort(),
                globalTeSettings.getMbExchange());
    }

    public static EiffelMessageBus initializeAndConnect(String reportMbDomainId, String mbHostWithPort, String mbExchange) {
        if (genericBus != null) {
            return genericBus;
        }

        EiffelMessageBus messageBus = new EiffelMessageBus(reportMbDomainId);
        messageBus.connect(mbHostWithPort, mbExchange);

        return messageBus;
    }

    // For test purposes
    public static void setGenericBus(EiffelMessageBus mb) {
        genericBus = mb;
    }

}
