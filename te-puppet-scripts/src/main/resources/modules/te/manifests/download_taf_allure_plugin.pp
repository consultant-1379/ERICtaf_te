define te::download_taf_allure_plugin($nexus_api_url, $taf_allure_plugins_version, $allure_plugin_dir) {

  $download_command = "sudo /usr/bin/wget --no-proxy '${nexus_api_url}&g=com.ericsson.cifwk&a=${title}&v=${taf_allure_plugins_version}' -O $allure_plugin_dir/${title}.jar"

  exec{"download_${title}":
    provider => shell,
    command  => $download_command,
    creates  =>  "${allure_plugin_dir}/${title}.jar",
    tries => 3
  }

#  cron { "set up regular ${title} upgrade":
#    ensure  => present,
#    command => $download_command,
#    user    => root,
#    hour    => 0,
#    minute  => 0
#  }
}