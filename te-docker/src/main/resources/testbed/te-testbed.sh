#!/usr/bin/env bash

echo "[INFO] Setting hostname"
if [ ! -n $HOSTNAME ]
then
   HOSTNAME=`hostname`
fi
export HOSTNAME=$HOSTNAME
echo "[INFO] HOSTNAME is set to $HOSTNAME"

echo "[INFO] Stopping & deleting TE containers"
runningContainers=`docker ps | grep taf_te`
echo $runningContainers
if [ -n "${runningContainers}" ]
  then
  docker-compose -f docker-compose-testbed.yml down
else
  echo "[INFO] There are no TE containers running"
fi

echo "[INFO] Starting TE containers (except slaves)"
docker-compose -f docker-compose-testbed.yml up --force-recreate -d

echo "sleeping for 30 seconds to ensure containers start"

sleep 30

echo "Checking Any containers in exited status"

COUNT=`docker ps -f status=exited | wc -l`

if [[ ${COUNT} > 1 ]]
then
    echo "Found container in exited staus"
    docker ps -f status=exited
    exit 1
else
    echo "No containers have exited"
fi
