#!/bin/sh

apt-get update && apt-get install -y alien
wget --no-proxy --no-check-certificate https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/EMC-Clariion-Storage/NaviSecCli/7.33.1.0.33/NaviSecCli-7.33.1.0.33.rpm -O /var/tmp/navicli.rpm
alien -d --script -i /var/tmp/navicli.rpm
rm -f /var/tmp/navicli.rpm
/opt/Navisphere/bin/naviseccli security -certificate -setLevel low