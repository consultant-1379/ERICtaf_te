#!/bin/bash

function escapeUrl {
    echo "$1" | sed 's:/:\\/:g'
}

# Server URL, used in OpenStack-based K8S clusters
k8s_server_url="https://kubernetes.default.svc.dekstroza.local"

for var in "$@"
do
    if [[ ${var} == "--k8s-server-url="* ]]
    then
        k8s_server_url=${var#*=}
    fi
done

grid_slave_version=${TE_SLAVE_IMAGE_VERSION}

echo "Grid slave version: ${grid_slave_version}"
echo "K8S server URL: ${k8s_server_url}"

sed -i "s/\$SETUP__GRID_TE_SLAVE_VERSION/${grid_slave_version}/" /usr/share/jenkins/ref/init.groovy.d/kubernetes_plugin_setup.groovy
# Escape // in URL
escaped_k8s_server_url="$(escapeUrl "${k8s_server_url}")"
sed -i "s/\$SETUP__K8S_SERVER_URL/${escaped_k8s_server_url}/" /usr/share/jenkins/ref/init.groovy.d/kubernetes_plugin_setup.groovy