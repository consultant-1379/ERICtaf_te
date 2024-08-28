#!/usr/bin/env bash
# Exit with code 1 if any subsequent command returns erroneous exit code
trap "exit 1" ERR
echo -e "\nInstalling everything needed for reporting..."

ftp_server=$1
common_http_proxy_host=$2
common_http_proxy_port=$3
allure_cli_dir=/opt/allure-cli
te_jenkins_logs_variable_name=TE_JENKINS_LOGS_SCAN_PATH
te_jenkins_logs_variable_value='\$4\/te-console-logs'
upload_script_folder=/opt/log_upload/

mkdir ~/.ssh
ssh-keyscan ${ftp_server} >> ~/.ssh/ftp_key
cat ~/.ssh/ftp_key >> ~/.ssh/known_hosts

#
# Install Allure - TODO: Delete Allure CLI tool installation once Allure Service adopted!
#
wget -O /tmp/allure-cli.zip "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/artifact/maven/redirect?r=releases&g=com.ericsson.de&a=te-allure-cli-tool&v=1.0.1&p=zip"
mkdir ${allure_cli_dir}
unzip /tmp/allure-cli.zip -d ${allure_cli_dir}
rm -f /tmp/allure-cli.zip
chmod +x ${allure_cli_dir}/bin/allure

#
# Set up Allure - TODO: Delete Allure CLI tool configuration once Allure Service adopted!
# Copy plugins to user's home directory to power them
#
mv ${allure_cli_dir}/.allure/ ~/
echo "export ALLURE_HOME=${allure_cli_dir}" >> ~/.bash_profile
# Update the report generation shell script
if [[ -z ${common_http_proxy_host} ]]
then
new_allure_java_invocation="export ${te_jenkins_logs_variable_name}=${te_jenkins_logs_variable_value}; java"
else
new_allure_java_invocation="export ${te_jenkins_logs_variable_name}=${te_jenkins_logs_variable_value}; java -Dhttps.proxyHost=${common_http_proxy_host} -Dhttps.proxyPort=${common_http_proxy_port} -Dhttp.proxyHost=${common_http_proxy_host} -Dhttp.proxyPort=${common_http_proxy_port}"
fi
sed -i "s/.*java .*/${new_allure_java_invocation} -jar \${ALLURE_CLI_JAR} \$@/" ${allure_cli_dir}/bin/allure

# Setup TE log uploading
mkdir ${upload_script_folder}
chmod 755 ${upload_script_folder}
pwd
cd /te_setup
chmod +x log_upload.sh
cp log_upload.sh ${upload_script_folder}upload
