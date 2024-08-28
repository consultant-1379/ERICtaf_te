#!/bin/bash
sed -i "s/-cp \/usr\/share\/jenkins\/slave\.jar/-cp \/usr\/share\/jenkins\/slave\.jar:\/te_setup\/taf_te_slave\.jar/" /usr/local/bin/jenkins-slave