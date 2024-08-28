package taf_te_master.setup_files.jenkins_init_scripts

import jenkins.model.Jenkins

println "*** Reloading Jenkins"
Jenkins.instance.reload()