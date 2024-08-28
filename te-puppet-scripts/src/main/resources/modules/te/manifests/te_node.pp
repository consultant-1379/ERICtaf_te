class te::te_node(
$te_node_version          = hiera('te_jenkins_version'),
$te_node_rpm_url          = hiera('te_node_rpm_url'),
$set_java_home            = hiera('te_node_set_java_home'),
$set_java7_home           = hiera('te_node_set_java7_home'),
$set_java8_home           = hiera('te_node_set_java8_home'),
$java8_home               = hiera('te_node_java8_home'),
$java8_tar_download_url   = hiera('java8_tar_download_url'),
$java8_tar_download_as    = '/tmp/java8.tar.gz',
$set_m2_home              = hiera('te_node_set_m2_home'),
$set_path                 = hiera('te_node_set_path'),
$te_node_user             = hiera('te_node_user'),
$te_logs                  = hiera('te_logs'),
$ip_jenkins               = hiera('ip_jenkins'),
$common_http_proxy_host     = hiera('common_http_proxy_host'),
$common_http_proxy_port     = hiera('common_http_proxy_port'),
$common_https_proxy_host    = hiera('common_https_proxy_host'),
$common_https_proxy_port    = hiera('common_https_proxy_port'),
$common_http_no_proxy       = hiera('common_http_no_proxy')
) {

# Ensure paths are set correctly
file_line {
  'root_JAVA_HOME':
  path    => '/root/.bash_profile',
  line    => "export ${set_java_home}";
  'root_JAVA7_HOME':
  path    => '/root/.bash_profile',
  line    => "export ${set_java7_home}";
  'root_JAVA8_HOME':
  path    => '/root/.bash_profile',
  line    => "export ${set_java8_home}";
  'root_M2_HOME':
  path    => '/root/.bash_profile',
  line    => "export ${set_m2_home}";
  'root_PATH':
  path    => '/root/.bash_profile',
  line    => "export ${set_path}",
  require => File_line['root_JAVA_HOME','root_JAVA7_HOME','root_JAVA8_HOME','root_M2_HOME'];
  "${te_node_user}_JAVA_HOME":
  path    => "/home/${te_node_user}/.bash_profile",
  line    => "export ${set_java_home}";
  "${te_node_user}_JAVA7_HOME":
  path    => "/home/${te_node_user}/.bash_profile",
  line    => "export ${set_java7_home}";
  "${te_node_user}_JAVA8_HOME":
  path    => "/home/${te_node_user}/.bash_profile",
  line    => "export ${set_java8_home}";
  "${te_node_user}_M2_HOME":
  path    => "/home/${te_node_user}/.bash_profile",
  line    => "export ${set_m2_home}";
  "${te_node_user}_PATH":
  path    => "/home/${te_node_user}/.bash_profile",
  line    => "export ${set_path}",
  require => File_line["${te_node_user}_JAVA_HOME","${te_node_user}_JAVA7_HOME","${te_node_user}_JAVA8_HOME","${te_node_user}_M2_HOME"];
} ->

# Download Java 8
exec { 'download Java 8':
  provider => shell,
  command  => "/usr/bin/wget --no-proxy --no-check-certificate \"${java8_tar_download_url}\" -O ${java8_tar_download_as}",
  creates  =>  $java8_tar_download_as
} ->

# Install Java 8
exec { 'install Java 8':
  provider => shell,
  command  => "tar -zxf /tmp/java8.tar.gz --directory /usr/java",
  creates  =>  $java8_home
} ->

# Output folder with correct rights
file { ['/opt/ericsson/',
        '/opt/ericsson/com.ericsson.cifwk.taf.executor/',
        '/opt/ericsson/com.ericsson.cifwk.taf.executor/workspace/']:
  ensure  => directory,
  owner   => $te_node_user,
  group   => $te_node_user,
  mode    => '0644',
  recurse => true,
} ->

exec { 'get te_node version':
  provider => shell,
  onlyif   => "[ \"$(rpm -qa | grep -i te-node)\" ] && exit 0 || exit 1",
  command  => "rpm -qa | grep -i te-node > /var/tmp/te_node_version"
} ->

exec { 'remove_old_node_version':
  provider => shell,
  onlyif   => "[ ! \"$(grep ${te_node_version} /var/tmp/te_node_version)\" ] && [ \"$(rpm -qa | grep -i te-node)\" ] && exit 0 || exit 1",
  command  => "rpm -e $(cat /var/tmp/te_node_version)"
} ->

# Install node from Nexus
package { "te-node-${te_node_version}.noarch":
  ensure   => installed,
  provider => 'rpm',
  source   =>  $te_node_rpm_url
} ->

file_line { "Set serviceUser=${te_node_user}":
    path    => '/etc/init.d/te-node',
    line    => "serviceUser=\"${te_node_user}\"",
    match   => 'serviceUser=\".*\"'
} ->

file_line { "Set serviceGroup=${te_node_user}":
    path    => '/etc/init.d/te-node',
    line    => "serviceGroup=\"${te_node_user}\"",
    match   => 'serviceGroup=\".*\"'
} ->

# Ensure service is started
exec { 'start_te_node':
  unless      => '/usr/bin/pgrep -f "te-node"',
  path        => [ "/bin/", "/sbin/" , "/usr/bin/", "/usr/sbin/" ],
  environment => ["${set_java_home}", "${set_m2_home}", "${set_path}"],
  command     => '/bin/bash -c "/etc/init.d/te-node start"'
}

cron { "Make sure log mount is running":
  ensure  => present,
  command => "sshfs ${ip_jenkins}:${te_logs} ${te_logs} -C -o reconnect,allow_other,umask=000,workaround=all",
  user    => root,
  minute  => "*/5"
}

file_line { "Setup http_proxy":
path    => '/etc/environment',
line    => "http_proxy=http://${common_http_proxy_host}:${common_http_proxy_port}";
}

file_line { "Setup https_proxy":
path    => '/etc/environment',
line    => "https_proxy=http://${common_https_proxy_host}:${common_https_proxy_port}";
}

file_line { "Setup no_proxy":
path    => '/etc/environment',
line    => "no_proxy=${common_http_no_proxy}";
}

file_line { "Make IPv4 the prefered":
path    => '/etc/gai.conf',
line    => "precedence ::ffff:0:0/96  100";
}

file_line { "Give lciadm100 privileges to install packages":
path    => '/etc/sudoers',
line    => "Defaults:lciadm100 !requiretty
lciadm100 ALL=(ALL) NOPASSWD: ALL ";
} ->

file_line { "Give lciadm100 privileges to install packages":
path    => '/etc/sudoers',
line    => "lciadm100 ALL=(ALL) NOPASSWD: ALL ";
}

file { "/etc/profile.d/set_proxy.sh":
    content => inline_template("
export http_proxy=http://${common_http_proxy_host}:${common_http_proxy_port}
export https_proxy=http://${common_https_proxy_host}:${common_https_proxy_port}
export no_proxy=${common_http_no_proxy}")
}

package {'python-pip':
  ensure   => installed,
  provider => 'yum'
} ->

exec { 'install pexpect 3.3 with pip':
  provider    => shell,
  command     => "pip install pexpect==3.3"
}

package {'python-requests':
  ensure   => installed,
  provider => 'yum'
}

package {'python-virtualenv':
  ensure   => installed,
  provider => 'yum'
}

exec { 'download NaviCli':
  provider => shell,
  command  => "/usr/bin/wget --no-proxy --no-check-certificate https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/EMC-Clariion-Storage/NaviSecCli/7.33.1.0.33/NaviSecCli-7.33.1.0.33.rpm -O /var/tmp/navicli.rpm",
  creates  =>  "/var/tmp/navicli.rpm"
} ->

exec { 'install NaviCli rpm':
   provider   => shell,
   unless     => "/bin/rpm -qa | grep -i nav",
   command    => "/bin/rpm -i /var/tmp/navicli.rpm",
} ->

exec { 'set NaviCli security level':
  provider    => shell,
  command     => "/opt/Navisphere/bin/naviseccli security -certificate -setLevel low"
}

package {'gcc':
  ensure   => installed,
  provider => 'yum'
} ->

package {'libxslt-devel':
  ensure   => installed,
  provider => 'yum'
} ->

exec { 'install lxml 3.2.4 with pip':
  provider    => shell,
  command     => "pip install lxml==3.2.4"
}

exec { 'install beautifulsoup with pip':
  provider    => shell,
  command     => "pip install beautifulsoup4"
}

exec { 'install paramiko with pip':
  provider    => shell,
  command     => "pip install paramiko==1.18.1"
}

}