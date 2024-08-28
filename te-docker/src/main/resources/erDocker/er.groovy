package groovy

import com.ericsson.duraci.eiffelmessage.binding.exceptions.EiffelMessageConsumptionException
import com.ericsson.duraci.eiffelmessage.binding.exceptions.RecoverableEiffelMessageConsumptionException
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage
import com.ericsson.eiffel.er.consumer.ERConsumer

public class StandardERScript extends ERConsumer {

    void consumeMessage(EiffelMessage message) throws EiffelMessageConsumptionException,
            RecoverableEiffelMessageConsumptionException {
        log.debug(String.format("Received %s (eventId: %s, consumer: %s).",
                message.eventType, message.eventId.toString(), name))

        // Store all messages
        dataStore.store(message)

        // Create denormalizations for Artifacts and Baselines
        switch (message.eventType) {
            case "EiffelArtifactNewEvent":
                dataStore.createArtifact(message)
                break
            case "EiffelBaselineDefinedEvent":
                dataStore.createBaseline(message)
                break
            case "EiffelConfidenceLevelModifiedEvent":
                dataStore.updateConfidenceLevel(message)
        }
    }

    @Override
    public String getName() {
        // To get the default ER queue name
        return "DefaultConsumer"
    }

    @Override
    public int getConsumers() {
        return 2
    }

    @Override
    List<String> getBindingKeys() {
        return new ArrayList<String>()
    }
}
