#!/bin/bash

function logger {
    echo "[INFO] "$1

}

echo "=================================="
echo "   Setting up TE Node Environment - Starting"
echo "=================================="

logger "Using apt-get to install libraries required for installing alterative version of python"
apt-get update && apt-get install -y wget curl -y gcc -y make -y zlib1g -y \
make build-essential libssl-dev zlib1g-dev libbz2-dev \
libreadline-dev libsqlite3-dev wget curl llvm libncurses5-dev libncursesw5-dev \
xz-utils tk-dev libffi-dev liblzma-dev python-openssl git libcairo2-dev libjpeg-dev libgif-dev \
libgirepository1.0-dev

logger "Installing alternative version of python"
if [ ! -f /var/tmp/Python-3.8.0.tgz ] || [ $(stat -c %s /var/tmp/Python-3.8.0.tgz) == 0 ]
then
    logger "Downloading tarfile"
    wget -O /var/tmp/Python-3.8.0.tgz -q http://www.python.org/ftp/python/3.8.0/Python-3.8.0.tgz > /var/tmp/Python-3.8.0.tgz
else
    logger "Tar file already downloaded, nothing to do"
fi

if [ ! -d /var/tmp/Python-3.8.0 ]
then
    logger "Extracting tar file"
    tar -zxf /var/tmp/Python-3.8.0.tgz --directory /var/tmp/
else
    logger "Tar file already extracted, nothing to do"
fi

python3.8 --version
result=$?

if [ $result -ne 0 ]
then
    logger "Python 3.8 not installed, installing now"
    chown root /home/jenkins/
    cd /var/tmp/Python-3.8.0/
    chown root /home/jenkins/
    ./configure --enable-optimizations
    make altinstall
else
    logger "Python 3.8 already installed, nothing to do"
fi

logger "Setting python version to 3.8.0"
rm -r /usr/bin/python
ln -s /usr/local/bin/python3.8 /usr/bin/python

logger "Using curl to install pip"

curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
python3.8 get-pip.py

logger "Using pip to install virtualenv, requests"
pip3.8 install astroid
pip3.8 install certifi
pip3.8 install chardet
pip3.8 install colorama
pip3.8 install cookies
pip3.8 install coverage
pip3.8 install idna
pip3.8 install isort
pip3.8 install lazy-object-proxy
pip3.8 install mccabe
pip3.8 install mock
pip3.8 install modernize
pip3.8 install nose
pip3.8 install pip
pip3.8 install pycodestyle
pip3.8 install pylint
pip3.8 install requests
pip3.8 install responses
pip3.8 install setuptools
pip3.8 install six
pip3.8 install typed-ast
pip3.8 install urllib3
pip3.8 install virtualenv
pip3.8 install wheel
pip3.8 install wrapt
pip3.8 install backports.functools-lru-cache
pip3.8 install beautifulsoup4
pip3.8 install configobj
pip3.8 install cryptography
pip3.8 install ecdsa
pip3.8 install ipaddress
pip3.8 install keyring
pip3.8 install keyrings.alt
pip3.8 install lxml
pip3.8 install mercurial
pip3.8 install paramiko
pip3.8 install pexpect
pip3.8 install pyasn1
pip3.8 install pycrypto
pip3.8 install pygobject
pip3.8 install pyOpenSSL
pip3.8 install pyxdg
pip3.8 install SecretStorage
pip3.8 install soupsieve
pip3.8 install enum34
pip3.8 install pyyaml

echo "=================================="
echo "   Setting up TE Node Environment - Finished"
echo "=================================="
