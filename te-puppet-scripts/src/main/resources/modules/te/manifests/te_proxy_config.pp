class te::te_proxy_config(
$http_proxy_host = hiera('common_http_proxy_host'),
$http_proxy_port = hiera('common_http_proxy_port'),
){

anchor { 'te::te_proxy_config::begin': } ~>

# Add proxy config to wget / Jenkins
file_line { 'wget_http_proxy':
  path => '/etc/wgetrc',
  line => "http_proxy = http://${http_proxy_host}:${http_proxy_port}/",
  match => "^http_proxy \=.*"
} ->

file_line { 'wget_https_proxy':
  path => '/etc/wgetrc',
  line => "https_proxy = http://${http_proxy_host}:${http_proxy_port}/",
  match => "^https_proxy \=.*"
} ->

# Add proxy config to curl / Graphana, Saiku
file { '/root/.curlrc' :
    ensure  => file,
} ->

file_line { 'curl_proxy':
  path => '/root/.curlrc',
  line => "proxy = ${http_proxy_host}:${http_proxy_port}",
  match => "^proxy \=.*"
} ->

# Add proxy config to Yum / Graphite, Collectd
ini_setting { 'yum_proxy':
  ensure  => present,
  path    => '/etc/yum.conf',
  section => 'main',
  setting => 'proxy',
  value   => "http://${http_proxy_host}:${http_proxy_port}/",
} ~>

anchor { 'te::te_proxy_config::end': }

}

