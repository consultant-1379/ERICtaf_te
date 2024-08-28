#!/usr/bin/env bash

apt-get remove -y openjdk-8-jdk openjdk-8-jdk-headless openjdk-8-jre libplexus-containers-java openjdk-8-jre-headless

#accept oracle licence
echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections

#set up repository that has oracle java
echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee /etc/apt/sources.list.d/webupd8team-java.list
echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list

wget "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/artifact/maven/redirect?r=releases&g=com.oracle&a=jdk&v=1.8.0_121&c=x64&e=tar.gz" -O /tmp/jdk1.8.0_121.tar.gz
tar -zxf /tmp/jdk1.8.0_121.tar.gz --directory /opt && rm -f /tmp/jdk1.8.0_121.tar.gz

export JDK_INSTALL=/opt/jdk1.8.0_121

#JDK 6 and above

update-alternatives --install /usr/bin/appletviewer appletviewer $JDK_INSTALL/bin/appletviewer 1
update-alternatives --install /usr/bin/extcheck extcheck $JDK_INSTALL/bin/extcheck 1
update-alternatives --install /usr/bin/idlj idlj $JDK_INSTALL/bin/idlj 1
update-alternatives --install /usr/bin/jar jar $JDK_INSTALL/bin/jar 1
update-alternatives --install /usr/bin/jarsigner jarsigner $JDK_INSTALL/bin/jarsigner 1
update-alternatives --install /usr/bin/java java $JDK_INSTALL/bin/java 1
update-alternatives --install /usr/bin/javac javac $JDK_INSTALL/bin/javac 1
update-alternatives --install /usr/bin/javadoc javadoc $JDK_INSTALL/bin/javadoc 1
update-alternatives --install /usr/bin/javah javah $JDK_INSTALL/bin/javah 1
update-alternatives --install /usr/bin/javap javap $JDK_INSTALL/bin/javap 1
update-alternatives --install /usr/bin/javaws javaws $JDK_INSTALL/bin/javaws 1
update-alternatives --install /usr/bin/jconsole jconsole $JDK_INSTALL/bin/jconsole 1
update-alternatives --install /usr/bin/jdb jdb $JDK_INSTALL/bin/jdb 1
update-alternatives --install /usr/bin/jhat jhat $JDK_INSTALL/bin/jhat 1
update-alternatives --install /usr/bin/jinfo jinfo $JDK_INSTALL/bin/jinfo 1
update-alternatives --install /usr/bin/jmap jmap $JDK_INSTALL/bin/jmap 1
update-alternatives --install /usr/bin/jps jps $JDK_INSTALL/bin/jps 1
update-alternatives --install /usr/bin/jrunscript jrunscript $JDK_INSTALL/bin/jrunscript 1
update-alternatives --install /usr/bin/jsadebugd jsadebugd $JDK_INSTALL/bin/jsadebugd 1
update-alternatives --install /usr/bin/jstack jstack $JDK_INSTALL/bin/jstack 1
update-alternatives --install /usr/bin/jstat jstat $JDK_INSTALL/bin/jstat 1
update-alternatives --install /usr/bin/jstatd jstatd $JDK_INSTALL/bin/jstatd 1
update-alternatives --install /usr/bin/keytool keytool $JDK_INSTALL/bin/keytool 1
update-alternatives --install /usr/bin/native2ascii native2ascii $JDK_INSTALL/bin/native2ascii 1
update-alternatives --install /usr/bin/orbd orbd $JDK_INSTALL/bin/orbd 1
update-alternatives --install /usr/bin/pack200 pack200 $JDK_INSTALL/bin/pack200 1
update-alternatives --install /usr/bin/policytool policytool $JDK_INSTALL/bin/policytool 1
update-alternatives --install /usr/bin/rmic rmic $JDK_INSTALL/bin/rmic 1
update-alternatives --install /usr/bin/rmid rmid $JDK_INSTALL/bin/rmid 1
update-alternatives --install /usr/bin/rmiregistry rmiregistry $JDK_INSTALL/bin/rmiregistry 1
update-alternatives --install /usr/bin/schemagen schemagen $JDK_INSTALL/bin/schemagen 1
update-alternatives --install /usr/bin/serialver serialver $JDK_INSTALL/bin/serialver 1
update-alternatives --install /usr/bin/servertool servertool $JDK_INSTALL/bin/servertool 1
update-alternatives --install /usr/bin/tnameserv tnameserv $JDK_INSTALL/bin/tnameserv 1
update-alternatives --install /usr/bin/unpack200 unpack200 $JDK_INSTALL/bin/unpack200 1
update-alternatives --install /usr/bin/wsgen wsgen $JDK_INSTALL/bin/wsgen 1
update-alternatives --install /usr/bin/wsimport wsimport $JDK_INSTALL/bin/wsimport 1
update-alternatives --install /usr/bin/xjc xjc $JDK_INSTALL/bin/xjc 1

update-alternatives --set appletviewer $JDK_INSTALL/bin/appletviewer
update-alternatives --set extcheck $JDK_INSTALL/bin/extcheck
update-alternatives --set idlj $JDK_INSTALL/bin/idlj
update-alternatives --set jar $JDK_INSTALL/bin/jar
update-alternatives --set jarsigner $JDK_INSTALL/bin/jarsigner
update-alternatives --set java $JDK_INSTALL/bin/java
update-alternatives --set javac $JDK_INSTALL/bin/javac
update-alternatives --set javadoc $JDK_INSTALL/bin/javadoc
update-alternatives --set javah $JDK_INSTALL/bin/javah
update-alternatives --set javap $JDK_INSTALL/bin/javap
update-alternatives --set javaws $JDK_INSTALL/bin/javaws
update-alternatives --set jconsole $JDK_INSTALL/bin/jconsole
update-alternatives --set jdb $JDK_INSTALL/bin/jdb
update-alternatives --set jhat $JDK_INSTALL/bin/jhat
update-alternatives --set jinfo $JDK_INSTALL/bin/jinfo
update-alternatives --set jmap $JDK_INSTALL/bin/jmap
update-alternatives --set jps $JDK_INSTALL/bin/jps
update-alternatives --set jrunscript $JDK_INSTALL/bin/jrunscript
update-alternatives --set jsadebugd $JDK_INSTALL/bin/jsadebugd
update-alternatives --set jstack $JDK_INSTALL/bin/jstack
update-alternatives --set jstat $JDK_INSTALL/bin/jstat
update-alternatives --set jstatd $JDK_INSTALL/bin/jstatd
update-alternatives --set keytool $JDK_INSTALL/bin/keytool
update-alternatives --set native2ascii $JDK_INSTALL/bin/native2ascii
update-alternatives --set orbd $JDK_INSTALL/bin/orbd
update-alternatives --set pack200 $JDK_INSTALL/bin/pack200
update-alternatives --set policytool $JDK_INSTALL/bin/policytool
update-alternatives --set rmic $JDK_INSTALL/bin/rmic
update-alternatives --set rmid $JDK_INSTALL/bin/rmid
update-alternatives --set rmiregistry $JDK_INSTALL/bin/rmiregistry
update-alternatives --set schemagen $JDK_INSTALL/bin/schemagen
update-alternatives --set serialver $JDK_INSTALL/bin/serialver
update-alternatives --set servertool $JDK_INSTALL/bin/servertool
update-alternatives --set tnameserv $JDK_INSTALL/bin/tnameserv
update-alternatives --set unpack200 $JDK_INSTALL/bin/unpack200
update-alternatives --set wsgen $JDK_INSTALL/bin/wsgen
update-alternatives --set wsimport $JDK_INSTALL/bin/wsimport
update-alternatives --set xjc $JDK_INSTALL/bin/xjc


#JDK 7 and above
update-alternatives --install /usr/bin/jcmd jcmd $JDK_INSTALL/bin/jcmd 1

update-alternatives --set jcmd $JDK_INSTALL/bin/jcmd


#JDK 8 only
update-alternatives --install /usr/bin/jjs jjs $JDK_INSTALL/bin/jjs 1
update-alternatives --install /usr/bin/jdeps jdeps $JDK_INSTALL/bin/jdeps 1

update-alternatives --set jjs $JDK_INSTALL/bin/jjs
update-alternatives --set jdeps $JDK_INSTALL/bin/jdeps