FROM armdocker.rnd.ericsson.se/proj_taf_te/te_base_images/python:3.7.5-stretch
RUN apt-get update
RUN apt-get install -y -q ldap-utils openjdk-8-jdk
RUN pip3.7 install simplejson
RUN pip3.7 install --upgrade virtualenv
RUN pip3.7 install flask

RUN mkdir /aat_setup/
COPY app.py /aat_setup
WORKDIR /aat_setup/

CMD python3.7 app.py