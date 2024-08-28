#!/usr/bin/env bash

destination_message_bus="mb1s11-eiffel004.eiffel.gic.ericsson.se"
exchange="mb1s11-eiffel004-shovel01"
queue="eiffel004.ext.tafuser.test.execution.Shovel.eiffel004.seli.taf"
binding_key="#.eiffel004.seli.taf"

for var in "$@"
do
    if [[ ${var} == "--destination-message-bus-address="* ]]
    then
        destination_message_bus=${var#*=}
        echo "Destination Message Bus is set to ${destination_message_bus}"
    elif [[ ${var} == "--exchange="* ]]
    then
        exchange=${var#*=}
        echo "Exchange is set to ${exchange}"
    elif [[ ${var} == "--queue="* ]]
    then
        queue=${var#*=}
        echo "Queue is set to ${queue}"
    elif [[ ${var} == "--binding_key="* ]]
    then
        binding_key=${var#*=}
        echo "Binding Key is set to ${binding_key}"
    fi
done

sed -i "s/\${DESTINATION_MB}/${destination_message_bus}/" /etc/rabbitmq/rabbitmq.config
sed -i "s/\${QUEUE}/${queue}/" /etc/rabbitmq/rabbitmq.config

sed -i "s/\${QUEUE}/${queue}/g" /etc/rabbitmq/definitions.json
sed -i "s/\${EXCHANGE}/${exchange}/g" /etc/rabbitmq/definitions.json
sed -i "s/\${BINDING_KEY}/${binding_key}/g" /etc/rabbitmq/definitions.json

/usr/local/bin/docker-entrypoint.sh rabbitmq-server