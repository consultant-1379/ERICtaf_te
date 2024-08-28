package taf_te_master.setup_files.jenkins_init_scripts

import hudson.model.AbstractItem
import hudson.model.TopLevelItem
import jenkins.model.Jenkins

import javax.xml.transform.stream.StreamSource

final jenkins = Jenkins.getInstance()

println "Creating TE jobs"

println "1. Creating TE Scheduler job"
createProject(jenkins, 'TEST_SCHEDULER')

println "2. Creating TE Executor job"
createProject(jenkins, 'TEST_EXECUTOR')

println "3. Creating full environment TE Executor job"
createProject(jenkins, 'FULL_ENV_TEST_EXECUTOR')

private def createProject(Jenkins jenkins, String name) {
    final item = jenkins.getItemByFullName(name, AbstractItem.class)
    FileInputStream configXml = new FileInputStream('/te_setup/jobs/' + name + '/config.xml')
    if (item != null) {
        item.updateByXml(new StreamSource(configXml))
        return item
    } else {
        TopLevelItem project = jenkins.createProjectFromXML(name, configXml)
        println "Created " + project
        project.save()
        return project
    }
}