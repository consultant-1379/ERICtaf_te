FROM armdocker.rnd.ericsson.se/proj_taf_te/te_base_images/jenkins/jenkins:2.186

MAINTAINER PDLTAFRAME@pdl.internal.ericsson.com

USER root

ARG ALLURE_HOME=/opt/allure-cli
ARG secondsToWaitForSuiteXmls=15
ARG TE_PLUGIN_ARTIFACT_VERSION=RELEASE
ARG ARM_REPOSITORY=releases

ENV ALLURE_HOME ${ALLURE_HOME}

RUN wget "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/3pptools/com/oracle/jdk/1.8.0_401/jdk-1.8.0_401-x64.tar.gz" -O /tmp/java8.tar.gz
RUN tar -zxf /tmp/java8.tar.gz --directory /opt && rm -f /tmp/java8.tar.gz
RUN echo 'JAVA8_HOME=/opt/jdk1.8.0_401 >> /etc/environment'

ENV JAVA_HOME=/opt/jdk1.8.0_401
ENV JAVA8_HOME=/opt/jdk1.8.0_401
ENV PATH=/opt/jdk1.8.0_401/bin:$PATH

ENV CURL_OPTIONS="-sSfL --insecure"

# To allow TE job params and to prevent setup wizard
ENV JAVA_OPTS -Dhudson.model.ParametersAction.keepUndefinedParameters=true -Djenkins.install.runSetupWizard=false -Djenkins.security.ClassFilterImpl.SUPPRESS_WHITELIST=true

# Creating necessary users, directories and installing required OS packages
# Need sshpass to upload Allure reports
RUN sed -i s/deb.debian.org/archive.debian.org/g /etc/apt/sources.list
RUN sed -i 's|security.debian.org|archive.debian.org/|g' /etc/apt/sources.list
RUN sed -i '/stretch-updates/d' /etc/apt/sources.list
RUN sed -i '/updates/d' /etc/apt/sources.list

RUN usermod -g root jenkins; \
    mkdir /var/log/jenkins; \
    chown -R jenkins:jenkins /var/log/jenkins; \
    mkdir /te_setup; \
    mkdir /usr/share/jenkins/ref/plugins/; \
    mkdir /usr/share/jenkins/ref/jobs/; \
    apt-get update && apt-get install sshpass

COPY setup_files /te_setup/

RUN openssl pkcs12 -export -in /te_setup/public.pem \
    -inkey /te_setup/private.pem \
    -passout pass:jenkins \
    -name "jenkinscert" \
    -out /te_setup/mycert.pfx

RUN keytool -importkeystore -srckeystore /te_setup/mycert.pfx \
    -destkeystore /te_setup/jenkins.jks \
    -srcstoretype pkcs12 \
    -deststoretype JKS \
    -srcstorepass jenkins \
    -noprompt -srcalias jenkinscert \
    -deststorepass jenkins

# Installing Jenkins plugins available in Jenkins repo, setting up TE reporting
RUN /usr/local/bin/install-plugins.sh build-flow-plugin:0.20 swarm:3.4 ldap:1.20 matrix-auth:2.3
RUN mv /te_setup/config.xml /usr/share/jenkins/ref/ && rm -rf /te_setup/config.xml; \
    mv /te_setup/jenkins_init_scripts/* /usr/share/jenkins/ref/init.groovy.d/ && rm -rf /te_setup/jenkins_init_scripts; \
    mv /te_setup/te_reporting/* /te_setup/ && rm -rf /te_setup/te_reporting; \
    chmod +x /te_setup/te_reporting_setup.sh; \
    chmod +x /te_setup/master_startup.sh; \
    chmod +x /te_setup/replace_open-jdk_oracle.sh; \
    chmod +x /te_setup/python_install.sh

RUN /te_setup/replace_open-jdk_oracle.sh
RUN /te_setup/python_install.sh

# Adding TE plugin
ADD https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/artifact/maven/redirect?r=${ARM_REPOSITORY}&g=com.ericsson.cifwk.taf.executor&a=te-jenkins-plugin&v=${TE_PLUGIN_ARTIFACT_VERSION}&p=hpi /usr/share/jenkins/ref/plugins/te-jenkins-plugin.hpi

# Overriding the ENTRYPOINT in Jenkins Dockerfile, because we need to preconfigure Jenkins with
# the params passed via cmdline (like Jenkins DNS) before we start it
ENTRYPOINT ["/bin/tini", "--", "/te_setup/master_startup.sh"]