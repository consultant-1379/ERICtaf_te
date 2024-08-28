#!/usr/bin/env bash

apt-key adv --keyserver keyserver.ubuntu.com --recv-keys C2518248EEA14886

sed -i s/deb.debian.org/archive.debian.org/g /etc/apt/sources.list
sed -i 's|security.debian.org|archive.debian.org/|g' /etc/apt/sources.list
sed -i '/stretch-updates/d' /etc/apt/sources.list
sed -i '/updates/d' /etc/apt/sources.list

apt-get update && \
        DEBIAN_FRONTEND=noninteractive apt-get update &&
        apt-get install \
        ca-certificates \
        curl \
        gnupg \
        lsb-release --yes
        mkdir -m 0755 -p /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

        chmod a+r /etc/apt/keyrings/docker.gpg
        apt-get update

        echo \
        "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
        $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get install apt-transport-https --yes
apt-get update
apt-get install docker-ce docker-ce-cli containerd.io --yes

curl https://baltocdn.com/helm/signing.asc | gpg --dearmor | tee /usr/share/keyrings/helm.gpg > /dev/null
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/helm.gpg] https://baltocdn.com/helm/stable/debian/ all main" | tee /etc/apt/sources.list.d/helm-stable-debian.list
apt-get update
apt-get install helm --yes

docker --version