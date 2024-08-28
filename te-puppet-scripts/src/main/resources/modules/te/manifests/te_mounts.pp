class te::te_mounts(
$te_logs             = hiera('te_logs'),
$ip_jenkins          = hiera('ip_jenkins'),
$te_node_user        = hiera('te_node_user')
) {

anchor { 'te::te_mounts::begin': } ->

#create directory for logs
file { $te_logs:
  ensure  => directory,
  mode    => '0777',
  recurse => true,
}

package {'fuse-sshfs':
  require  => File[$te_logs],
  ensure   => installed,
  provider => 'yum'
}

#add ftp to known hosts
exec { 'Create key':   
	    require  => Package['fuse-sshfs'], 
      provider => shell,      
      command  => "ssh-keyscan ${ip_jenkins} >> /root/.ssh/ftp_key",
      creates  => "/root/.ssh/ftp_key"
} ->

exec { 'Add Key to known hosts root':    
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

anchor { 'te::te_mounts::end': }

}