#!/bin/sh

apt-get install -y python-dev python-pip python-requests python-virtualenv
pip install pexpect==3.3
pip install lxml
pip install beautifulsoup4
pip install paramiko==1.18.1