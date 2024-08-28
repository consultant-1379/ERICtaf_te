#!/usr/bin/env sh

# Build and install
runuser -l vagrant -c 'cd /vagrant/te-node && mvn clean package -Prpm'
rpm -Uvh /vagrant/te-node/target/rpm/te-node/RPMS/noarch/te-node-*.noarch.rpm
