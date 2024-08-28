package taf_te_master.setup_files.jenkins_init_scripts

import jenkins.model.Jenkins

def executorCount = ${MASTER_EXECUTOR_COUNT}
println "*** Setting the master Jenkins executors number to " + executorCount
Jenkins.instance.setNumExecutors(executorCount)
Jenkins.instance.setQuietPeriod(0)