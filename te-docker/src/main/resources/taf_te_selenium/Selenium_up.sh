#!/bin/bash

echo "[INFO] Deleting any Selenium containers that might be running and pruning volumes"
if [[ $(docker ps -aq --filter "name=selhub") ]]
then
docker rm -f $(docker ps -aq --filter "name=selhub")
fi

if [[ $(docker ps -aq --filter "name=selnode") ]]
then
docker rm -f $(docker ps -aq --filter "name=selnode")
fi

if [[ $(docker ps -aq --filter "name=sikulinode") ]]
then
docker rm -f $(docker ps -aq --filter "name=sikulinode")
fi
if [[ $(docker network ls | grep "isolated_nw" | wc -l) == 0 ]]
then
  docker network create --driver bridge isolated_nw
fi
docker run --network=isolated_nw --name selhub -p 4444:4444 -dit taf_selhub:1.0.5
docker run --network=isolated_nw --name selnode -dit taf_selnode:1.0.5
docker run --network=isolated_nw --name sikulinode -dit taf_sikulinode:1.0.5

HubId=$(docker ps -aqf "name=selhub")
docker exec -i $HubId /bin/bash -c 'IP=`hostname -i`;sed -i "2 s/.*/  \"host\":${IP},/g" /home/seluser/hub.json'
docker exec -d $HubId /bin/bash -c '/home/seluser/start-selenium-hub.sh'

HubIPAddress=`docker inspect   -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $HubId`

sleep 10

echo "[INFO] Selenium hub container is up"


SelNodeId=$(docker ps -aqf "name=selnode")
docker exec -i $SelNodeId /bin/bash -c 'IP=`hostname -i`;sed -i "19 s/.*/        \"host\":${IP},/g" /home/seluser/node-1.json'
docker exec -i $SelNodeId /bin/bash -c "sed -i '26 s/.*/        \"hubHost\":$HubIPAddress,/g' /home/seluser/node-1.json"
docker exec -d $SelNodeId /bin/bash -c "xvfb-run '--server-args=:11.0 -screen 0 1360x1020x24 -ac +extension RANDR' /home/seluser/start-selenium-node.sh 1 :11.0"
docker exec -d $SelNodeId /bin/bash -c 'PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin DISPLAY=:11.0 x11vnc -forever -shared -rfbport 5900 -display :11.0'

sleep 10

echo "[INFO] Selenium node container is up"

SikulinodeId=$(docker ps -aqf "name=sikulinode")
docker exec -i $SikulinodeId /bin/bash -c 'IP=`hostname -i`;sed -i "13 s/.*/        \"host\":${IP},/g" /home/seluser/node-1.json'
docker exec -i $SikulinodeId /bin/bash -c "sed -i '20 s/.*/        \"hubHost\":$HubIPAddress,/g' /home/seluser/node-1.json"
docker exec -d $SikulinodeId /bin/bash -c "xvfb-run '--server-args=:11.0 -screen 0 1360x1020x24 -ac +extension RANDR' /home/seluser/start-selenium-node.sh 1 :11.0"
docker exec -d $SikulinodeId /bin/bash -c 'PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin DISPLAY=:11.0 x11vnc -forever -shared -rfbport 5900 -display :11.0'

sleep 10

echo "[INFO] Sikulinode container is up"