#!/bin/bash

function logger {
    echo "[INFO] "$1
}

echo "=================================="
echo "   Setting up TE Node Environment - Starting"
echo "=================================="

logger "Using yum & rpm to install libraries required for installing alterative version of python"
yum groupinstall -y -q 'development tools'
yum install -y -q openssl-devel sqlite-devel bzip2-devel
rpm -ivh http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm

logger "Installing alternative version of python"
if [ ! -f /var/tmp/Python-2.7.8.tgz ] || [ $(stat -c %s /var/tmp/Python-2.7.8.tgz) == 0 ]
then
    logger "Downloading tarfile"
    wget -O /var/tmp/Python-2.7.8.tgz -q http://www.python.org/ftp/python/2.7.8/Python-2.7.8.tgz > /var/tmp/Python-2.7.8.tgz
else 
    logger "Tar file already downloaded, nothing to do"
fi

if [ ! -d /var/tmp/Python-2.7.8 ]
then
    logger "Extracting tar file"
    tar -zxf /var/tmp/Python-2.7.8.tgz --directory /var/tmp/
else
    logger "Tar file already extracted, nothing to do"
fi


python2.7 --version
result=$?

if [ $result -ne 0 ] 
then
    logger "Python 2.7 not installed, installing now"   
    cd /var/tmp/Python-2.7.8/    
    ./configure
    make 
    make altinstall 
else
    logger "Python 2.7 already installed, nothing to do"
fi

logger "Using yum to install pip" 
yum  install -y -q python-pip

logger "Using pip to install virtualenv, requests"
pip install virtualenv
pip install requests

echo "=================================="
echo "   Setting up TE Node Environment - Finished"
echo "=================================="
