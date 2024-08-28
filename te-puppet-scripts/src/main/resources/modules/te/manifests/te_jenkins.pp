class te::te_jenkins(
$te_jenkins_version   = hiera('te_jenkins_version'),
$te_plugin_url        = hiera('te_jenkins_plugin_url'),
$proxy_http_host      = hiera('common_http_proxy_host'),
$proxy_http_port      = hiera('common_http_proxy_port'),
$jenkins_url          = hiera('te_jenkins_url'),
$port_jenkins         = hiera('port_jenkins'),
$port_jenkins_ajp     = hiera('port_jenkins_ajp'),
$te_jenkins_node_name = hiera('te_jenkins_node_name'),
$set_java_home        = hiera('te_master_set_java_home'),
$te_node_user         = hiera('te_node_user'),

$te_logs              = hiera('te_logs'),
$log_host             = hiera('log_host'),
$upload_folder        = hiera('upload_folder'),
$te_jenkins_version   = hiera('te_jenkins_version'),
$allure_version_pom   = hiera('allure_version_pom'),
$min_executor_disk_space_gb = hiera('min_executor_disk_space_gb'),

$reporting_mb_host      = hiera('reporting_mb_host'),
$reporting_mb_port      = hiera('reporting_mb_port'),
$reporting_mb_user_name = hiera('reporting_mb_user_name'),
$reporting_mb_user_pwd  = hiera('reporting_mb_user_pwd'),
$reporting_mb_exchange  = hiera('reporting_mb_exchange'),
$reporting_mb_domain_id = hiera('reporting_mb_domain_id'),

$deletable_flows_age_in_days = hiera('deletable_flows_age_in_days')
) {  

require te_proxy_config
require te_reporting

$java_home_add_to_env_cmd = "export ${set_java_home}"
$java_home_add_to_path_cmd = 'export PATH=$PATH:$JAVA_HOME/bin'

anchor { 'te::te_jenkins::begin': } ->

# Ensure paths are set correctly
file_line {
  'root_master_JAVA_HOME':
    path    => '/root/.bash_profile',
    line    => $java_home_add_to_env_cmd;
  'root_master_PATH':
    path    => '/root/.bash_profile',
    line    => $java_home_add_to_path_cmd,
    require => File_line['root_master_JAVA_HOME'];
  "${te_node_user}_master_JAVA_HOME":
    path    => "/home/${te_node_user}/.bash_profile",
    line    => $java_home_add_to_env_cmd;
  "${te_node_user}_master_PATH":
    path    => "/home/${te_node_user}/.bash_profile",
    line    => $java_home_add_to_path_cmd,
    require => File_line["${te_node_user}_master_JAVA_HOME"];
} ->

# Open port
firewall { "100 open ${port_jenkins} port-jenkins":
  port   => $jenkins_port,
  proto  => tcp,
  action => accept,
} ->

# Install Jenkins
class { 'jenkins':
  require            => Class['te_proxy_config'],
  configure_firewall => false,
  proxy_host         => $proxy_http_host,
  proxy_port         => $proxy_http_port,
  config_hash        => {
    'HTTP_PORT'        => { 'value' => $port_jenkins },
    'JENKINS_PORT'     => { 'value' => $port_jenkins },
    'JENKINS_AJP_PORT' => { 'value' => $port_jenkins_ajp }
  }
}

$jenkins_plugin_name = "te-jenkins-plugin-${te_jenkins_version}.hpi"

# Configure Jenkins
file { '/var/lib/jenkins/jenkins.model.JenkinsLocationConfiguration.xml':
  ensure  => file,
  owner   => 'jenkins',
  mode    => '0644',
  content => template('te/jenkins/jenkins.model.JenkinsLocationConfiguration.xml'),
  notify  => Service['jenkins'],
} ->

# Create & Configure Jobs
file { ['/var/lib/jenkins/jobs','/var/lib/jenkins/jobs/TEST_EXECUTOR','/var/lib/jenkins/jobs/TEST_SCHEDULER', '/var/tmp/jenkins_plugins']:
  ensure  => directory,
  owner   => 'jenkins',
  mode    => '0644',
  recurse => true,
} ->

# Install TE plugin from Nexus
exec { 'download_te_jenkins_plugin':
  provider => shell,
  command  => "/usr/bin/wget --no-proxy '${te_plugin_url}' -O /var/tmp/jenkins_plugins/${jenkins_plugin_name}",
  creates  =>  "/var/tmp/jenkins_plugins/${jenkins_plugin_name}"
}

exec { 'remove_old_jenkins_plugin':
  require  => Exec['download_te_jenkins_plugin'],
  provider => shell,
  onlyif   => "[ \"$(ls /var/lib/jenkins/plugins | grep ${jenkins_plugin_name})\" ] && exit 1 || exit 0",
  command  => "rm -f /var/lib/jenkins/plugins/te-jenkins-plugin-*.hpi"
}

file { "/var/lib/jenkins/plugins/${jenkins_plugin_name}" :
  require => Exec['remove_old_jenkins_plugin'],
  source  => "/var/tmp/jenkins_plugins/${jenkins_plugin_name}",
  owner   => 'jenkins',
  mode    => '0644',
  notify  => Service ['jenkins'],
} ->

file { '/var/lib/jenkins/jobs/TEST_EXECUTOR/config.xml':
  ensure  => file,
  owner   => 'jenkins',
  mode    => '0644',
  content => template('te/jenkins/jobs/TEST_EXECUTOR/config.xml'),
  notify  => Service ['jenkins'],
} ->

file_line { 'Jenkins Website root path':
    ensure  => present,
    line    => 'JENKINS_ARGS="--prefix=/jenkins"',
    path    => '/etc/sysconfig/jenkins',
    match   => 'JENKINS_ARGS=',
} ->

file { '/var/lib/jenkins/jobs/TEST_SCHEDULER/config.xml':
  ensure  => file,
  owner   => 'jenkins',
  mode    => '0644',
  content => template('te/jenkins/jobs/TEST_SCHEDULER/config.xml'),
  notify  => Service ['jenkins'],
}

#Get jenkins cli jar
exec { 'get jenkins cli jar':
  provider => shell,
  onlyif   => "[ \"$(ls /var/tmp/ | grep jenkins-cli.jar)\" ] && exit 1 || exit 0",
  command  => "/usr/bin/wget --no-proxy http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar -O /var/tmp/jenkins-cli.jar",
  creates  =>  "/var/tmp/jenkins-cli.jar"
}

#jenkins - reload configuration
exec { 'jenkins reload-config':
  require  => Exec['get jenkins cli jar'],
  provider => shell,
  command  => "/usr/bin/java -jar /var/tmp/jenkins-cli.jar -s http://localhost:8080/jenkins reload-configuration",
}

# Install Jenkins plugins from repository
jenkins::plugin {
  'build-flow-plugin' :
      version => '0.20';
  'swarm' :
      version => '3.4';
} ->

anchor { 'te::te_jenkins::end': }

}
