#!/usr/bin/env sh

WORK_DIR=/tmp/te-node
mkdir $WORK_DIR

# Ericsson proxy
export http_proxy=http://153.88.253.150:8080/
export no_proxy=localhost,127.0.0.1
echo "proxy=$http_proxy" >> /etc/yum.conf
cat >> /etc/bashrc << EOF
export http_proxy=$http_proxy
export no_proxy=$no_proxy
EOF

# Maven 3.2.1
java -version
wget -P $WORK_DIR http://apache.petsads.us/maven/maven-3/3.2.1/binaries/apache-maven-3.2.1-bin.tar.gz
tar -C $WORK_DIR -xzf $WORK_DIR/apache-maven-3.2.1-bin.tar.gz
mkdir -p /opt/apache
mv $WORK_DIR/apache-maven-3.2.1 /opt/apache/maven
cat >> /etc/bashrc << EOF
export JAVA_HOME=/usr
export M2_HOME=/opt/apache/maven
export PATH=\$M2_HOME/bin:\$PATH
EOF

# RPM Build
yum -y install rpm-build

# Cleanup
rm -rf $WORK_DIR
