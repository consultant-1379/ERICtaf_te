<head>
    <title>Message Bus Problems</title>
</head>

# Message bus problems

There's an internal message bus (MB) that is used in TE vApp for Eiffel test event forwarding to EIS MB. In case there
are any problems with it you can go and restart the RabbitMQ service on **tafexemb1** VM in [TE vApp](te_vapp.html).

If you see messages like `INFO: Eiffel/MessageSender: Failed to create version 2.2.3.5.21 event instance corresponding to EiffelTestSuiteStartedEventImpl.
It will not be serialized for this version. Reason: Expected event implementation`, don't worry: this is a message
from Eiffel messaging library (used in TE) that informs you that it didn't find the current event in Eiffel messaging
library version 2 - only in version 3. This means that if there's a message consumer that works with old Eiffel library version,
it will fail to deserialize this message. Just ignore this message.
