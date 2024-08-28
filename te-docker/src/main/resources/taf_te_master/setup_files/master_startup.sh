#!/usr/bin/env bash

function printEscapedUrl {
    echo "$1" | sed 's:/:\\/:g'
}

echo "Starting up TE master..."

jenkins_sh_location="/usr/local/bin/jenkins.sh"
current_host_address=`hostname -I | cut -d ' ' -f1`
external_jenkins_address="http://${current_host_address}:8080/jenkins"
local_report_host_base_uri="https://oss-taf-logs.seli.wh.rnd.internal.ericsson.com"
upload_to_oss_logs="true"
message_bus_host="taf_te_message_bus"
logstore_dir=/proj/PDU_OSS_CI_TAF_LOG
logstore_user=tafuser
logstore_pw=wREbruHERUqas4aw
logstore_ftp=seliiuts02214.seli.gic.ericsson.se
allure_service_url=''
allure_service_backend_url=''
executor_count=2
setup_proxy=true
HTTP_PROXY_HOST=atproxy1.athtem.eei.ericsson.se
HTTP_PROXY_PORT=3128
ALLURE_REPORTS_SERVER=seliiuts02214.seli.gic.ericsson.se
LOG_UPLOAD_PATH="/opt/log_upload"

# We have to have it separately, because jenkins.sh fails on params it doesn't know, so can't just pass $@ to it
params_for_jenkins_startup=()

for var in "$@"
do
    if [[ ${var} == "--turn-off-reporting="* ]]
    then
        turn_off_reporting=${var#*=}
    elif [[ ${var} == "--hostname="* ]]
    then
        hostname=${var#*=}
        echo "Hostname is set to ${hostname}."
    elif [[ ${var} == "--external-jenkins-address="* ]]
    then
        external_jenkins_address=${var#*=}
        echo "External_jenkins_address is set to ${external_jenkins_address}."
    elif [[ ${var} == "--local-report-host-base-uri="* ]]
    then
        local_report_host_base_uri=${var#*=}
        upload_to_oss_logs="false"
        echo "Local reporting will be used, base URI is ${local_report_host_base_uri}. No reports will be uploaded to OSS logs."
    elif [[ ${var} == "--setup-proxy="* ]]
    then
        setup_proxy=${var#*=}
        echo "proxy is set to: ${setup_proxy}"
    elif [[ ${var} == "--message-bus-host="* ]]
    then
        message_bus_host=${var#*=}
        echo "Will use message bus host ${message_bus_host}."
    elif [[ ${var} == "--ftp_log_storage="* ]]
    then
        logstore_dir=${var#*=}
        echo "FTP Log Storage: ${logstore_dir}."
    elif [[ ${var} == "--log_storage_user="* ]]
    then
        logstore_user=${var#*=}
        echo "log_storage_user: ${logstore_user}."
    elif [[ ${var} == "--log_storage_user_pw="* ]]
    then
        logstore_pw=${var#*=}
        echo "log_storage_user_pw: ${logstore_pw}."
    elif [[ ${var} == "--ftp_host="* ]]
    then
        logstore_ftp=${var#*=}
        echo "ftp_host: ${logstore_ftp}."
    elif [[ ${var} == "--report_url="* ]]
    then
        local_report_host_base_uri=${var#*=}
        echo "report_url: ${local_report_host_base_uri}."
    elif [[ ${var} == "--allure_service_url="* ]]
    then
        allure_service_url=${var#*=}
        echo "allure_service_url: ${allure_service_url}."
    elif [[ ${var} == "--allure_service_backend_url="* ]]
    then
        allure_service_backend_url=${var#*=}
        echo "allure_service_backend_url: ${allure_service_backend_url}."
    elif [[ ${var} == "--executor-count="* ]]
    then
        executor_count=${var#*=}
        echo "Executor count defined: ${executor_count}"
    else
        params_for_jenkins_startup+=("${var} ")
    fi
done

if [ "$ENABLE_LDAP" == 'true' ]
then
echo "LDAP is enabled. So configuring Jenkins with the LDAP configuration"
if [ $status != 0 ]; then
   echo "Copy Code: $status - Copy of config.xml for Jenkins is Unsuccessful"
fi
sed -i "s/HOSTNAME/${hostname}/g" /usr/share/jenkins/ref/config.xml
else
rm -rf /usr/share/jenkins/ref/config.xml
fi

echo "Setting up Reporting"
if [[ ${turn_off_reporting} == true ]]
then
    mkdir ${LOG_UPLOAD_PATH}
    cp /te_setup/log_upload_turned_off.sh ${LOG_UPLOAD_PATH}/upload
    chmod +x ${LOG_UPLOAD_PATH}/upload
else
    if [[ ${setup_proxy} == true ]]
    then
        /te_setup/te_reporting_setup.sh ${ALLURE_REPORTS_SERVER} ${HTTP_PROXY_HOST} ${HTTP_PROXY_PORT}
    else
        /te_setup/te_reporting_setup.sh ${ALLURE_REPORTS_SERVER}
    fi
fi

# Escape // in external address
escaped_external_jenkins_address="$(printEscapedUrl "${external_jenkins_address}")"
sed -i "s/\$EXTERNAL_JENKINS_ADDRESS/${escaped_external_jenkins_address}/" /usr/share/jenkins/ref/init.groovy.d/set_dns.groovy
sed -i "s/\${MASTER_EXECUTOR_COUNT}/${executor_count}/" /usr/share/jenkins/ref/init.groovy.d/executors.groovy

escaped_local_report_host_base_uri="$(printEscapedUrl "${local_report_host_base_uri}")"
escaped_allure_service_url="$(printEscapedUrl "${allure_service_url}")"
escaped_allure_service_backend_url="$(printEscapedUrl "${allure_service_backend_url}")"
sed -i "s/\${ALLURE_REPORTS_HOST}/${escaped_local_report_host_base_uri}/" /te_setup/jobs/TEST_SCHEDULER/config.xml
sed -i "s/\${ALLURE_SERVICE_URL}/${escaped_allure_service_url}/" /te_setup/jobs/TEST_SCHEDULER/config.xml
sed -i "s/\${ALLURE_SERVICE_BACKEND_URL}/${escaped_allure_service_backend_url}/" /te_setup/jobs/TEST_SCHEDULER/config.xml
sed -i "s/\${UPLOAD_TO_OSS_LOGS}/${upload_to_oss_logs}/" /te_setup/jobs/TEST_SCHEDULER/config.xml
sed -i "s/\${MESSAGE_BUS_HOST}/${message_bus_host}/" /te_setup/jobs/TEST_SCHEDULER/config.xml

if [[ ${turn_off_reporting} != true ]]
then
    escaped_logstore_dir="$(printEscapedUrl "${logstore_dir}")"
    sed -i "s/\${LOGSTORE_DIR}/${escaped_logstore_dir}/" ${LOG_UPLOAD_PATH}/upload
    sed -i "s/\${LOGSTORE_USER}/${logstore_user}/" ${LOG_UPLOAD_PATH}/upload
    sed -i "s/\${LOGSTORE_PW}/${logstore_pw}/" ${LOG_UPLOAD_PATH}/upload
    sed -i "s/\${LOGSTORE_FTP}/${logstore_ftp}/" ${LOG_UPLOAD_PATH}/upload
fi
echo "Starting up ${jenkins_sh_location} with parameters: ${params_for_jenkins_startup}"

${jenkins_sh_location} ${params_for_jenkins_startup} --httpsPort=443 --httpsKeyStore="/te_setup/jenkins.jks" --httpsKeyStorePassword="jenkins"
