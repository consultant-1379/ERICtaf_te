#!/usr/bin/env bash

while getopts v:a: option
do
    case "${option}"
	    in
	    v) TE_VERSION=${OPTARG};;
	    a) SCRIPT_ARGS=${OPTARG};;
	    esac
done
echo "[INFO] Version to download is $TE_VERSION"

TE_VERSION_ONVAPP=$(docker ps -a --filter "name=taf_te_master" --format "{{.Image}}" | awk -F ":" '{print $2}')
TE_EXITED=$(docker ps -a --filter status=exited | grep -v PORTS | wc -l)
if [[ ! -z ${TE_VERSION_ONVAPP} && ${TE_VERSION_ONVAPP} == ${TE_VERSION} ]]
then
echo "[INFO] TE is installed with ${TE_VERSION_ONVAPP}"
if [ ${TE_EXITED} != 0 ]
then
    echo "[INFO] One or more containers are not started.."
    docker ps -a
    docker stop $(docker ps -a -q)
    docker start $(docker ps -a -q)
fi
else
    wget "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/artifact/maven/redirect?r=public&g=com.ericsson.cifwk.taf.executor&a=te-docker&v=${TE_VERSION}&p=zip" -O te-docker.zip
    unzip -o te-docker.zip -d te-docker-files
    rm -f te-docker.zip
    cd te-docker-files
    chmod +x dockerized_te.sh
    ls -lah
    if [[ ${SCRIPT_ARGS} == *"docker-compose-te-three-slave-rnl-oss-logs.yml"* ]]
    then
        wget "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/cifwk/taf/ERICtafLdapClient/1.1.3/ERICtafLdapClient-1.1.3.tar.gz" -O ERICtafLdapClient.tar.gz
        cp -R $HOME/Selenium_images.tar .
            if [ $? == 0 ]
            then
                echo "[INFO] Selenium images found with name Selenium_images.tar in $HOME"
            else
                echo "[INFO] Selenium Images tar with name Selenium_images.tar is not available in $HOME location"
                echo "[INFO] Downloading from NEXUS....Takes time to download 7GB tar...."
                wget "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/cifwk/taf/Selenium/1.1.12/Selenium-1.1.12.tar" -O Selenium_images.tar
            fi
        docker load -i Selenium_images.tar
        if [[ $(docker network ls | grep "isolated_nw" | wc -l) == 0 ]]
        then
          docker network create --driver bridge isolated_nw
        fi
        wget "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/cifwk/taf/Selenium_script/1.1.2/Selenium_script-1.1.2.sh" -O Selenium_script.sh
        sh Selenium_script.sh
        sleep 10
        echo "[INFO] Initiating CRON..For more info please check the file cronCheck.txt in current directory!! "
        chmod +x cron_trigger.sh
        dos2unix cron_trigger.sh
        chmod +x delete_dir.sh
        dos2unix delete_dir.sh
        cp cron_trigger.sh $HOME/
        cp delete_dir.sh $HOME/
        echo "[INFO] Copying cron script to user's HOME & delete script to container!!"
        (crontab -l ; echo "0 */8 * * * ./cron_trigger.sh" ; ) | crontab -
     else
        chmod +x cache_cron.sh
        dos2unix cache_cron.sh
        chmod +x cache_clean.sh
        dos2unix cache_clean.sh
        cp cache_cron.sh cache_clean.sh $HOME/
        sh $HOME/cache_cron.sh
     fi

    ARM_URL="https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/groups/public"
    export ARM_URL=$ARM_URL
    echo "[INFO] ARM_URL is set to $ARM_URL"
    echo "[INFO] Args to pass to dockerized_te.sh are : $SCRIPT_ARGS"
    sudo rm -rf /etc/timezone
    sudo bash -c "echo Europe/Dublin > /etc/timezone "
    ./dockerized_te.sh ${SCRIPT_ARGS}
    echo "[INFO] Sleeping to prevent jenkins triggering health check too early"
fi
sleep 150

if [[ ${SCRIPT_ARGS} == *"docker-compose-te-three-slave-oss-logs.yml"* ]]
then
   REPO_FOLDER=/tmp/rfadependency

   echo "[INFO] Create repository folder if it does not exist"
   if [ ! -d "$REPO_FOLDER" ]
   then
     echo "[INFO] $REPO_FOLDER is not present, it will be created"
     mkdir -m 777 $REPO_FOLDER
   fi

   echo "[INFO] Downloading dependencies"
   cd $REPO_FOLDER
   curl http://taf.lmera.ericsson.se/taflanding/TE_dependencies/RFAdependencies.tar.gz -o RFAdependencies.tar.gz
   tar -xvf RFAdependencies.tar.gz
   rm -f RFAdependencies.tar.gz

   echo "[INFO] Copying dependencies to slaves"
   docker cp repository/ taf_te_slave1:/root/.m2/
   docker cp repository/ taf_te_slave2:/root/.m2/
   docker cp repository/ taf_te_slave3:/root/.m2/
fi