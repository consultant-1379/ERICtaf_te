package taf_te_master.setup_files.jenkins_init_scripts

import jenkins.model.Jenkins

def locationConfig = Jenkins.instance.getDescriptor('JenkinsLocationConfiguration')
def externalAddress="$EXTERNAL_JENKINS_ADDRESS"
println("*** Setting external Jenkins address to '" + externalAddress + "'")
locationConfig.setUrl(externalAddress)