Vagrant.configure("2") do |config|

  config.vm.box = "centos64_386"
  config.vm.box_url = "http://developer.nrel.gov/downloads/vagrant-boxes/CentOS-6.4-i386-v20130731.box"
  config.vm.provision "shell", path: "provision/bootstrap.sh"
  config.vm.provision "shell", path: "provision/test.sh"

end
