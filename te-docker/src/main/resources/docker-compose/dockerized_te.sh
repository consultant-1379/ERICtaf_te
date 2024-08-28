#!/usr/bin/env bash

function showHelp(){
    echo
    echo "Script to deploy TE"
    echo "  - pulls images from docker registry"
    echo "  - spins up containers using docker-compose"
    echo
    echo "Usage: $0 [option...]" >&2
    echo "  -h                          Show help"
    echo "  -n hostname                 Set hostname, defaults to machine hostname"
    echo "  -f compose-file             Set docker-compose file to use, defaults to 'docker-compose-te-one-slave.yml'"
    echo "  -c executor_count           Set the executor count on the TE slaves, defaults to '4'"
    echo "  -o offline                  Set the maven offline mode, default is 'false'"
    echo "  -u username                 Set LDAP TE username"
    echo "  -p password                 Set LDAP TE password"
    exit 1
}

while getopts h?:n:f:c:o:u:p: option
do
  case "${option}"
    in
    h|\?) showHelp;;
    n) HOSTNAME=${OPTARG};;
    f) COMPOSE_FILE=${OPTARG};;
    c) EXECUTOR_COUNT=${OPTARG};;
    o) MAVEN_OFFLINE=${OPTARG};;
    u) TE_USERNAME=${OPTARG};;
    p) TE_PASSWORD=${OPTARG};;
    esac
done


if [ ${COMPOSE_FILE} = "docker-compose-te-one-slave-agat.yml" ]
then
 if [[ -z ${TE_USERNAME} || -z ${TE_PASSWORD} ]]
then
echo "TE USERNAME/PASSWORD is empty. They are required for the provided YAML file"
showHelp
exit 1
fi
fi



echo "[INFO] Setting HOSTNAME variable"
if [ -z ${HOSTNAME} ]
then
   HOSTNAME=`hostname`
fi

echo "[INFO] Setting EXECUTOR_COUNT variable"
if [  ! -z ${EXECUTOR_COUNT} ];
then
   EXECUTOR_COUNT=$EXECUTOR_COUNT
elif [[  -z ${EXECUTOR_COUNT} && ${COMPOSE_FILE} == *"one-slave"*  ]];
then
   EXECUTOR_COUNT=4
else
   EXECUTOR_COUNT=6
fi

echo "[INFO] Setting MAVEN_OFFLINE variable"
if [ -z ${MAVEN_OFFLINE} ]
then
   MAVEN_OFFLINE="false"
fi
echo "[INFO] Setting docker-compose file"
if [ -z ${COMPOSE_FILE} ]
then
   COMPOSE_FILE=docker-compose-te-one-slave.yml
fi



if [[ ${COMPOSE_FILE} = "docker-compose-te-one-slave-agat.yml" || ${COMPOSE_FILE} = "docker-compose-te-three-slave-rnl-oss-logs.yml" || ${COMPOSE_FILE} = "docker-compose-te-ldap.yml" ]]
then
    for AatClient in `ls | grep "ERICtafAatClient" | grep "tar.gz"`;do
    tar -xvf $AatClient
    done
    for LdapClient in `ls | grep "ERICtafLdapClient" | grep "tar.gz"`; do
    tar -xvf $LdapClient
    echo "[INFO] Setting ldap-client"
    which ldapadd
    if [  $? == 1 ]
    then
      sudo rpm -ivh --replacefiles ./openldap_client_rpms/*.rpm
      if [  $? == 1 ]
      then
         echo "ldap-client installation failed"
         exit 1
      fi
    fi
    done
    if [ -z "$LdapClient" ]
    then
      echo "Bypassing installation of ldap-client"
    fi



if [ ${COMPOSE_FILE} = "docker-compose-te-one-slave-agat.yml" ]
then
echo "[INFO] Setting TE_USERNAME and TE_PASSWORD variables"
ldapwhoami -x -w $TE_PASSWORD -D uid=$TE_USERNAME,ou=People,dc=agat,dc=enm,dc=org -h localhost -p 3689 > /dev/null 2>&1
if [ $? != 0 ]
then
echo "TE USERNAME/PASSWORD provided are invalid. Please try again with valid USERNAME/PASSWORD"
exit 1
fi
fi


fi
export HOSTNAME=$HOSTNAME
echo "[INFO] HOSTNAME is set to $HOSTNAME"
export EXECUTOR_COUNT=$EXECUTOR_COUNT
echo "[INFO] EXECUTOR_COUNT is set to $EXECUTOR_COUNT"
export MAVEN_OFFLINE=$MAVEN_OFFLINE
echo "[INFO] MAVEN_OFFLINE is set to $MAVEN_OFFLINE"
echo "[INFO] docker-compose file is set to $COMPOSE_FILE"
export TE_USERNAME=$TE_USERNAME
export TE_PASSWORD=$TE_PASSWORD

sudo mkdir -p /var/log/allure-service
sudo chmod 777 "/var/log/allure-service"


echo "[INFO] Deleting any TE containers that might be running and pruning volumes"
if [[ $(docker ps -aq --filter "name=taf_te") ]]
then
docker rm -f $(docker ps -aq --filter "name=taf_te")
fi

if [[ $(docker ps -aq --filter "name=TDM") ]]
then
docker rm -f $(docker ps -aq --filter "name=TDM")
fi

if [[ $(docker ps -aq --filter "name=mongo") ]]
then
docker rm -f $(docker ps -aq --filter "name=mongo")
fi

if [[ $(docker ps -aq --filter "name=ldap") ]]
then
docker rm -f $(docker ps -aq --filter "name=ldap")
fi

if [[ $(docker ps -aq --filter "name=aat") ]]
then
docker rm -f $(docker ps -aq --filter "name=aat")
fi

echo "[INFO] Starting up TE"
docker-compose -f ${COMPOSE_FILE} up -d

sleep 15

if [[ $(docker network ls | grep "isolated_nw" | wc -l) == 0 ]]
then
  docker network create --driver bridge isolated_nw
fi

if [ ${COMPOSE_FILE} = "docker-compose-te-one-slave-agat.yml" ]
then
  sh Selenium_up.sh
fi

echo "[INFO] Setting up IPV6 tables"
sudo ip6tables -t nat -A POSTROUTING -s fd00::/80 ! -o docker0 -j MASQUERADE
sudo ip6tables -t nat -A POSTROUTING -s 2001:3984:3989::/64 ! -o docker0 -j MASQUERADE