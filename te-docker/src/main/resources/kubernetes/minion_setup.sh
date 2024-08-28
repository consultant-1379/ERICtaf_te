#!/usr/bin/env bash

# Creating directories to be used by Persistent Volumes
mkdir -p /var/te_data/taf-te-jenkins
mkdir -p /var/te_data/taf-te-logs

# To enable access from TE Jenkins container
chmod -R 777 /var/te_data/
setenforce 0
# Set SELINUX=disabled in /etc/selinux/config to make SELinux turned off by default