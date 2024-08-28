FROM armdocker.rnd.ericsson.se/proj_taf_te/te_base_images/jenkinsci/jnlp-slave:3.10-1

MAINTAINER PDLTAFRAME@pdl.internal.ericsson.com

USER root

EXPOSE 22

ARG TE_SLAVE_ARTIFACT_VERSION=RELEASE
ARG ARM_REPOSITORY=releases

RUN wget "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/artifact/maven/redirect?r=releases&g=com.oracle&a=jdk&v=1.7.0_80&c=x64&e=tar.gz" -O /tmp/java7.tar.gz
RUN tar -zxf /tmp/java7.tar.gz --directory /opt && rm -f /tmp/java7.tar.gz
RUN wget "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/3pptools/com/oracle/jdk/1.8.0_401/jdk-1.8.0_401-x64.tar.gz" -O /tmp/java8.tar.gz
RUN tar -zxf /tmp/java8.tar.gz --directory /opt && rm -f /tmp/java8.tar.gz
RUN echo 'JAVA8_HOME=/opt/jdk1.8.0_401 >> /etc/environment'
RUN echo 'JAVA7_HOME=/opt/jdk1.7.0_80 >> /etc/environment'


ENV M2_HOME=/usr/share/maven
ENV JAVA7_HOME=/opt/jdk1.7.0_80
ENV JAVA8_HOME=/opt/jdk1.8.0_401
ENV JAVA_HOME=/opt/jdk1.8.0_401
ENV PATH=/opt/jdk1.8.0_401/bin:$PATH

COPY *.sh /te_setup/
COPY jnlp_slave.xml /te_setup/

RUN chmod +x /te_setup/replace_open-jdk_oracle.sh
RUN /te_setup/replace_open-jdk_oracle.sh

RUN chmod +x /te_setup/docker_install.sh
RUN /te_setup/docker_install.sh

ENV PATH=/var/lib/docker:$PATH
ENV PATH=/bin/helm:$PATH

RUN apt-get update && \
    apt-get -y install maven && \
    mkdir -p /root/.m2
COPY maven/settings_template.xml /te_setup/maven/settings_template.xml

# Download and run JAR directly to avoid dealing with env vars and permissions for lciadm100
ADD https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/artifact/maven/redirect?r=${ARM_REPOSITORY}&g=com.ericsson.cifwk.taf.executor&a=te-node&v=${TE_SLAVE_ARTIFACT_VERSION}&p=jar /te_setup/taf_te_slave.jar
RUN chmod +x /te_setup/*.sh
RUN /te_setup/add_te_node_to_slave_classpath.sh

ENTRYPOINT ["/te_setup/start_te_slave.sh"]