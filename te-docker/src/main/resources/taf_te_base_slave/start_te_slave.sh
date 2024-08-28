#!/usr/bin/env bash

set -xv

echo I am user `whoami`

echo "1. Starting TE slave"

agent_label="taf"
create_jenkins_node=""
executor_count=3
te_master_location=""
desired_agent_name="$HOSTNAME"
disable_unique_client_id=""
disable_reconnection_to_master=""
reconnection_interval_in_seconds=""
arm_mirror="https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/groups/public"
maven_offline="<offline>false</offline>"
setup_proxy=true
PROXY_HOST=atproxy1.athtem.eei.ericsson.se
PROXY_PORT=3128
NO_PROXY_FOR=""

for var in "$@"
do
    if [[ ${var} == "--executor-count="* ]]
    then
        executor_count=${var#*=}
        echo "Executor count defined: ${executor_count}"
    elif [[ ${var} == "--te-master-address="* ]]
    then
        te_master_location=${var#*=}
        echo "TE master address defined: ${te_master_location}"
    elif [[ ${var} == "--arm_mirror="* ]]
    then
        arm_mirror=${var#*=}
        echo "ARM Mirror defined: ${arm_mirror}"
    elif [[ ${var} == "--agent-name="* ]]
    then
        desired_agent_name=${var#*=}
        echo "Desired agent name: ${desired_agent_name}"
    elif [[ ${var} == "--setup-proxy="* ]]
    then
        setup_proxy=${var#*=}
        echo "proxy is set to: ${setup_proxy}"
    elif [[ ${var} == "--create-jenkins-node="* ]]
    then
        create_jenkins_node=${var#*=}
        echo "Create Jenkins node: ${create_jenkins_node}"
    elif [[ ${var} == "--maven-offline="* ]]
    then
        maven_boolean=${var#*=}
        maven_offline="<offline>${maven_boolean}</offline>"
        echo "Maven Offline set to: ${maven_offline}"
    elif [[ ${var} == "--te-username="* ]]
    then
        te_username=${var#*=}
    elif [[ ${var} == "--te-password="* ]]
        then
            te_password=${var#*=}
    fi
done

# Master lookup is supported only in Swarm, so we need a definite address in case of plain JNLP agent
if [ -z "${te_master_location}" ];
then
    echo "TE master location is needed to be defined as a parameter (e.g., '--te-master-address=http://my-master:8080/jenkins/')"
    exit 1
fi

if [[ ${setup_proxy} == true ]]
then
    echo "Setting Proxy"
    /te_setup/proxy_setup.sh ${PROXY_HOST} ${PROXY_PORT} ${NO_PROXY_FOR}
fi

echo "2. Updating Maven Settings file"
sed "s@\${MIRROR_URL}@${arm_mirror}@" /te_setup/maven/settings_template.xml | sed "s@\${OFFLINE_MODE}@${maven_offline}@" > /root/.m2/settings.xml


agent_launching_script="/usr/local/bin/jenkins-slave"
if [ "true" = "${create_jenkins_node}" ]
then
    echo "Creating node on ${te_master_location}"
    node_xml_location="/te_setup/jnlp_slave.xml"

    sed -i "s/\${EXECUTOR_COUNT}/${executor_count}/" ${node_xml_location}
    sed -i "s/\${AGENT_LABEL}/${agent_label}/" ${node_xml_location}
    echo "XML for node creation:"
    cat ${node_xml_location}
    jenkins_cli="/tmp/jenkins-cli.jar"
    echo "Waiting for the master URL ${te_master_location} to become available to download Jenkins CLI"
    until $(wget -O ${jenkins_cli} ${te_master_location}/jnlpJars/jenkins-cli.jar); do
        echo "Jenkins master is not up, retrying in 5 seconds"
        sleep 5
    done
    echo "Giving TE master time to come up fully"
    sleep 15
    echo "Creating Jenkins node"
    if [ "$ENABLE_LDAP" == 'true' ]
    then
        command_to_create_node=$( cat ${node_xml_location} | java -jar ${jenkins_cli} -s ${te_master_location} -auth ${te_username}:${te_password} create-node ${desired_agent_name} )
        slaveComputerMac=`echo 'println jenkins.model.Jenkins.instance.nodesObject.getNode("'${desired_agent_name}'")?.computer?.jnlpMac' | java -jar ${jenkins_cli} -s ${te_master_location} -auth ${te_username}:${te_password} groovy =`
    else
        command_to_create_node=$( cat ${node_xml_location} | java -jar ${jenkins_cli} -s ${te_master_location} create-node ${desired_agent_name} )
        slaveComputerMac=`echo 'println jenkins.model.Jenkins.instance.nodesObject.getNode("'${desired_agent_name}'")?.computer?.jnlpMac' | java -jar ${jenkins_cli} -s ${te_master_location} groovy =`
    fi
    echo "Computer MAC for the JNLP agent is ${slaveComputerMac}"
    rm -f ${jenkins_cli}
    command_to_run_agent="${agent_launching_script} -url ${te_master_location} ${slaveComputerMac} ${desired_agent_name}"
    echo "Starting TE agent with command ${command_to_run_agent}"
    ${command_to_run_agent}
else
    echo "Starting TE agent with command ${agent_launching_script}"
    ${agent_launching_script}
fi