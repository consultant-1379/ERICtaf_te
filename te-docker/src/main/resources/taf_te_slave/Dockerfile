FROM armdocker.rnd.ericsson.se/proj_taf_te/taf_te_base_slave:${project.version}

MAINTAINER PDLTAFRAME@pdl.internal.ericsson.com

COPY *.sh /te_setup/
RUN chmod +x /te_setup/*.sh

# NaviCli
RUN /te_setup/navicli_install.sh

# Python
RUN /te_setup/python_install.sh

ENTRYPOINT ["/te_setup/start_te_slave.sh"]