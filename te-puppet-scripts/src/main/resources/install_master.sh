#!/bin/sh

echo "=================================="
echo "Installing TE Gateway - Starting"
echo "=================================="

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# Install puppet master
sudo rpm -ivh http://yum.puppetlabs.com/puppetlabs-release-el-6.noarch.rpm
sudo yum install -y puppet-server

# Set proxy
sudo puppet config set http_proxy_host atproxy1.athtem.eei.ericsson.se --section main
sudo puppet config set http_proxy_port 3128 --section main

# Enable nodes auto sign
sudo touch /etc/puppet/autosign.conf
echo '*.vts.com' | sudo tee --append /etc/puppet/autosign.conf

# Install necessary modules

sudo puppet module install puppetlabs-firewall --version=1.1.3
sudo puppet module install puppetlabs-mysql --version=2.3.1
sudo puppet module install puppetlabs-inifile --version=1.1.4

sudo puppet module install dwerder-graphite --version=5.5.0
sudo puppet module install rtyler-jenkins --version=1.2.0
sudo puppet module install nanliu-staging --version=0.4.1
sudo puppet module install pdxcat-collectd --version=3.0.1
sudo chmod 765 -R /etc/puppet/modules/collectd/
sudo puppet module install bfraser-grafana --version=0.1.3
sudo puppet module install gini/archive --version=0.2.0 --force
sudo puppet module install puppetlabs-rabbitmq
sudo puppet module install puppetlabs-ntp

# Create initial manifests
sudo cp $DIR/manifests/* /etc/puppet/manifests/
sudo cp -R $DIR/modules/* /etc/puppet/modules/

# Create hiera data
sudo mkdir -p /etc/puppet/hiera_data/
sudo cp $DIR/hiera_data/* /etc/puppet/hiera_data/
sudo cp $DIR/hiera.yaml /etc/puppet/

# Configure master via puppet
sudo puppet apply /etc/puppet/manifests/gateway.pp

# Add puppet to startup
sudo chkconfig puppetmaster off

# Start puppet server
# Commented out improve TE stability
#sudo puppet master

echo "=================================="
echo "   Installing TE Gateway - Finished"
echo "=================================="
