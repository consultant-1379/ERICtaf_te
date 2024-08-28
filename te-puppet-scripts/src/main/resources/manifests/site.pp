node 'tafexem1.vts.com' {
    include te::te_jenkins
    include ntp
}

node 'tafexes1.vts.com' {
    include te::te_mounts
    include te::te_node
    include ntp
}

node 'tafexes2.vts.com' {
    include te::te_mounts
    include te::te_node
    include ntp
}

node 'tafexemb1.vts.com' {
    include te::te_mounts
    include te::te_node
    include ntp
    include te::te_proxy_config
    include te::te_rabbitmq
}