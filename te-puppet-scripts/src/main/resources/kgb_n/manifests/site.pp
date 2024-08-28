node 'tafexem1.vts.com' {
  include te::te_jenkins
  include te::te_node
  include te::te_rabbitmq
  include ntp
}