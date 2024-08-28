FROM armdocker.rnd.ericsson.se/proj_taf_te/te_base_images/mongo:3.2

MAINTAINER PDLTAFRAME@pdl.internal.ericsson.com

RUN apt-get update && apt-get install -y curl
COPY initReplicas.sh /
RUN chmod +x /initReplicas.sh
CMD ["mongod","--replSet","my-mongo-set"]
