FROM armdocker.rnd.ericsson.se/proj_taf_te/taf_te_master:${project.version}

MAINTAINER PDLTAFRAME@pdl.internal.ericsson.com

ARG TE_SLAVE_IMAGE_VERSION="${project.version}"
#ARG K8S_PLUGIN_ARM_REPOSITORY=releases
#ARG K8S_PLUGIN_VERSION=RELEASE

# /te_setup exists in TE master image
COPY setup_files /te_setup/

RUN /usr/local/bin/install-plugins.sh kubernetes:0.12
RUN mv /te_setup/jenkins_init_scripts/* /usr/share/jenkins/ref/init.groovy.d/ && rm -rf /te_setup/jenkins_init_scripts
RUN chmod +x /te_setup/setup.sh
RUN /te_setup/setup.sh

# Download patched K8s plugin here; if downloaded in a RUN script that follows, it can't resolve arm101 server
#ADD https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/artifact/maven/redirect?r=${K8S_PLUGIN_ARM_REPOSITORY}&g=com.ericsson.de.jenkins.plugins&a=kubernetes&v=${K8S_PLUGIN_VERSION}&p=hpi /usr/share/jenkins/ref/plugins/kubernetes_plugin.hpi