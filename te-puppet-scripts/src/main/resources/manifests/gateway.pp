# Ensure that ports are forwarded
$port_jenkins            = hiera('port_jenkins')
$port_rabbitmq_cli       = hiera('port_rabbitmq_cli')

$ip_jenkins              = hiera('ip_jenkins')
$ip_rabbitmq             = hiera('ip_rabbitmq')
$puppet_report_max_age   = hiera('puppet_report_max_age')

firewall { '103 forward to Jenkins':
  chain => 'PREROUTING',
  proto => 'tcp',
  todest => "${ip_jenkins}:${port_jenkins}",
  dport =>  $port_jenkins,
  source => '! 192.168.0.1/24',
  jump => 'DNAT',
  table => 'nat',
}

firewall { '106 forward to RabbitMq':
  chain => 'PREROUTING',
  proto => 'tcp',
  todest => "${ip_rabbitmq}:${port_rabbitmq_cli}",
  dport => $port_rabbitmq_cli,
  source => '! 192.168.0.1/24',
  jump => 'DNAT',
  table => 'nat',
}

cron { "clean reports":
  ensure  => present,
  command => "cd /var/lib/puppet/reports && find . -type f -name *.yaml -mtime +${puppet_report_max_age} -print0 | xargs -0 -n50 /bin/rm -f",
  user    => root,
  hour    => "*/8"
}
