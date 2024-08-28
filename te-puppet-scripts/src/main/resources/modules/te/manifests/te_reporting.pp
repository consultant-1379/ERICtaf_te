class te::te_reporting(

$te_logs             = hiera('te_logs'),
$ftp_host            = hiera('ftp_host'),
$upload_sh           = hiera('upload_sh'),
$upload_folder       = hiera('upload_folder'),
$common_http_proxy_host   = hiera('common_http_proxy_host'),
$common_http_proxy_port   = hiera('common_http_proxy_port'),
$nexus_api_url       = hiera('nexus_api_url'),

$ftp_log_storage     = hiera('ftp_log_storage'),

$log_storage_user    = hiera('log_storage_user'),
$log_storage_user_pw = hiera('log_storage_user_pw'),
$seconds_to_wait_for_suite_xmls = hiera('seconds_to_wait_for_suite_xmls'),

$allure_cli_path            = hiera('allure_cli_path'),
$allure_cli_zip_url         = hiera('allure_cli_zip_url'),
$allure_cli_dir             = hiera('allure_cli_dir'),
$allure_cli_binary          = hiera('allure_cli_binary'),
$allure_dir_in_user_home    = hiera('allure_dir_in_user_home'),
$allure_plugin_dir          = hiera('allure_plugin_dir'),
$taf_allure_plugins_version = hiera('taf_allure_plugins_version'),

$te_node_user        = hiera('te_node_user'),

$te_jenkins_logs_variable_name   = hiera('te_jenkins_logs_plugin_scan_variable_name'),
$te_jenkins_logs_variable_value  = hiera('te_jenkins_logs_plugin_scan_variable_value')

) {

require te_proxy_config

$allure_home_add_to_env_cmd = "export ALLURE_HOME=${allure_cli_dir}"

anchor { 'te::te_reporting::begin': } ->

#create directory for logs
file { $te_logs:
  ensure  => directory,
  mode    => '0777',
  recurse => true,
} ->

file { "${te_logs}/empty":
  ensure  => file,
  mode    => '0777',
  recurse => true,
}

#add ftp to known hosts
exec { 'Scan ftp key':
      provider => shell,
      command  => "ssh-keyscan ${ftp_host} >> /root/.ssh/ftp_key",
      creates  => "/root/.ssh/ftp_key"
} ->

exec { 'Add ftp key to known hosts':
      provider => shell,
      onlyif   => "[ \"$(grep -f /root/.ssh/ftp_key /root/.ssh/known_hosts)\" ] && exit 1 || exit 0",
      command  => "cat /root/.ssh/ftp_key >> /root/.ssh/known_hosts",
} ->

file { ["/home/${te_node_user}", "/home/${te_node_user}/.ssh"]:
  ensure => directory
} ->

file {"/home/${te_node_user}/.ssh/known_hosts":
  ensure => file
} ->

exec { "Add Key to known hosts ${te_node_user}":
      provider => shell,
      onlyif   => "[ \"$(grep -f /root/.ssh/ftp_key /home/${te_node_user}/.ssh/known_hosts)\" ] && exit 1 || exit 0",
      command  => "cat /root/.ssh/ftp_key >> /home/${te_node_user}/.ssh/known_hosts",
} ->

# Remove previously downloaded allure cli zip
exec { 'remove_old_allure_cli_zip':
  provider => shell,
  user => root,
  command  => "rm -f /usr/src/allure-cli.zip",
  logoutput => true
} ->

#install allure-cli to folder
archive { 'allure-cli':
  ensure    => present,
  url       => $allure_cli_zip_url,
  target    => $allure_cli_dir,
  extension => 'zip',
  checksum  => false
} ->

# Set up Allure. It will create .allure dir in user home
exec { 'setup_allure':
  provider => shell,
  user => root,
  command  => "rm -rf ~/.allure; mv ${allure_cli_dir}/.allure/ ~/",
  logoutput => true
} ->

# Set Allure home as env property
file_line {
  'root_ALLURE_HOME':
    path    => '/root/.bash_profile',
    line    => $allure_home_add_to_env_cmd;
  "${te_node_user}_ALLURE_HOME":
    path    => "/home/${te_node_user}/.bash_profile",
    line    => $allure_home_add_to_env_cmd;
} ->

# Create a directory for Allure plugins
file { $allure_cli_binary:
  ensure  => file,
  mode    => '0755',
  recurse => true,
} ->

# Add proxy support
file_line { "add_proxy_to_allure_cli_call":
    path    => $allure_cli_binary,
    line    => "export ${te_jenkins_logs_variable_name}=${te_jenkins_logs_variable_value}; java -Dhttps.proxyHost=${common_http_proxy_host} -Dhttps.proxyPort=${common_http_proxy_port} -Dhttp.proxyHost=${common_http_proxy_host} -Dhttp.proxyPort=${common_http_proxy_port} -jar \${ALLURE_CLI_JAR} $@",
    match   => '.*java .*'
} ->

# Download TAF Allure plugins
download_taf_allure_plugin { "allure-commons":
  nexus_api_url => $nexus_api_url,
  taf_allure_plugins_version => $taf_allure_plugins_version,
  allure_plugin_dir => $allure_plugin_dir
} ->

download_taf_allure_plugin { "allure-csv-plugin":
  nexus_api_url => $nexus_api_url,
  taf_allure_plugins_version => $taf_allure_plugins_version,
  allure_plugin_dir => $allure_plugin_dir
} ->

download_taf_allure_plugin { "allure-priority-plugin":
  nexus_api_url => $nexus_api_url,
  taf_allure_plugins_version => $taf_allure_plugins_version,
  allure_plugin_dir => $allure_plugin_dir
} ->

download_taf_allure_plugin { "allure-jenkins-logs-plugin":
  nexus_api_url => $nexus_api_url,
  taf_allure_plugins_version => $taf_allure_plugins_version,
  allure_plugin_dir => $allure_plugin_dir
} ->

file { $upload_folder:
  ensure  => directory,
  mode    => '0777',
  recurse => true,
} ->

package {'sshpass':
  ensure   => installed,
  provider => 'yum'
} ->

#build log delivery script
file { $upload_sh:
  ensure  => file,
  mode    => '0777',
  content => template('te/logging/log_upload.sh'),
  recurse => true
} ->

#allows jenkins to execute sudo command without password
file { '/etc/sudoers.d/jenkins':
  ensure  => file,
  mode    => '0644',
  content => template('te/logging/jenkins_sudoer'),
}

anchor { 'te::te_reporting::end': }

}
