FROM armdocker.rnd.ericsson.se/proj_taf_te/te_base_images/tomcat:7.0-jre8

MAINTAINER PDLTAFRAME@pdl.internal.ericsson.com

COPY eventrepository.cfg /usr/local/tomcat/conf/
COPY rule /usr/local/tomcat/conf/
COPY er.groovy /usr/local/tomcat/conf/

RUN wget https://eiffel.lmera.ericsson.se/nexus/content/repositories/releases/com/ericsson/duraci/eventrepository/44.0.2/eventrepository-44.0.2.war -O /usr/local/tomcat/webapps/eventrepository.war
