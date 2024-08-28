FROM armdocker.rnd.ericsson.se/proj_taf_te/te_base_images/rabbitmq:3.6.12-management

MAINTAINER PDLTAFRAME@pdl.internal.ericsson.com

RUN sed -i s/deb.debian.org/archive.debian.org/g /etc/apt/sources.list
RUN sed -i 's|security.debian.org|archive.debian.org/|g' /etc/apt/sources.list
RUN sed -i '/stretch-updates/d' /etc/apt/sources.list
RUN sed -i '/updates/d' /etc/apt/sources.list

RUN apt-get update && apt-get install -y --no-install-recommends \
		python \
	&& rm -rf /var/lib/apt/lists/*

# copy configuration setup for shovel plugin
COPY config_files/rabbitmq.config /etc/rabbitmq/rabbitmq.config
COPY config_files/definitions.json /etc/rabbitmq/definitions.json

RUN rabbitmq-plugins enable --offline rabbitmq_management rabbitmq_shovel rabbitmq_shovel_management

RUN mkdir /te_message_bus_setup

COPY setup_files /te_message_bus_setup/

RUN chmod +x /te_message_bus_setup/message_bus_setup.sh
RUN chmod +x /te_message_bus_setup/python_install.sh
RUN /te_message_bus_setup/python_install.sh

ENTRYPOINT ["/te_message_bus_setup/message_bus_setup.sh"]

CMD ["rabbitmq-server"]