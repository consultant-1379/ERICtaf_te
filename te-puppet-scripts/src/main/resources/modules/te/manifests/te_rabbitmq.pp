class te::te_rabbitmq(
$port_rabbitmq          = hiera('port_rabbitmq'),
$port_rabbitmq_cli      = hiera('port_rabbitmq_cli'),
$with_shovel_plugin     = hiera('enable_shovel_plugin'),
$reporting_mb_exchange  = hiera('reporting_mb_exchange'),
$shovel_destination     = hiera('shovel_destination'),
$shovel_queue           = hiera('shovel_queue'),
$shovel_name            = hiera('shovel_name'),
) {

validate_bool($with_shovel_plugin)

if $with_shovel_plugin {
   $rabbitmq_config_template = 'te/rabbitmq/rabbitmq.config'
}

require te_proxy_config

package { 'epel-release':
  require  =>  File_Line['curl_proxy'],
  ensure   => 'installed',
  source   => 'http://download.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm',
  provider => 'rpm',
} ->

package { 'erlang':
  ensure  => 'installed',
  provider => 'yum',
} ->

class { '::rabbitmq':
  port    => "${port_rabbitmq}",
  config  => $rabbitmq_config_template,
} ->

# Configure firewall
firewall { "100 open ${port_rabbitmq} port":
  port   => $port_rabbitmq,
  proto  => tcp,
  action => accept,
} ->

# Configure firewall
firewall { "101 open ${port_rabbitmq_cli} port":
  port   => $port_rabbitmq_cli,
  proto  => tcp,
  action => accept,
} ->

Staging::File <| title == 'rabbitmqadmin' |> {
curl_option => '--noproxy localhost',
wget_option => '--no-proxy',
} 

if $with_shovel_plugin {

    validate_string($shovel_destination)
    validate_string($shovel_queue)

    rabbitmq_plugin {'rabbitmq_shovel':
      ensure => present,
      require => Class['rabbitmq::install'],
      notify  => Class['rabbitmq::service']
    }

    rabbitmq_plugin {'rabbitmq_shovel_management': 
      ensure  => present,
      require => Class['rabbitmq::install'],
      notify  => Class['rabbitmq::service']
    }

    rabbitmq_exchange { "${reporting_mb_exchange}@/":
      type       => 'topic',
      ensure     =>  present,
    }

    exec { 'Add Queue to rabbitmq':
          require  => Anchor['rabbitmq::end'],
          provider => shell,
          onlyif   => "[ \"$(/usr/local/bin/rabbitmqadmin list queues name | grep ${shovel_queue})\" ] && exit 1 || exit 0",
          command  => "/usr/local/bin/rabbitmqadmin declare queue name=${shovel_queue} durable=true",
    }
}


# Anchor this as per #8040 - this ensures that classes won't float off and
# mess everything up.  You can read about this at:
# http://docs.puppetlabs.com/puppet/2.7/reference/lang_containment.html#known-issues

anchor { 'te::te_rabbitmq::begin': } 
anchor { 'te::te_rabbitmq::end': }

}




