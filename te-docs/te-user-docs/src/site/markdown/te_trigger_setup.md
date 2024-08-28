<head>
    <title>TE Trigger Setup</title>
</head>

# TE Trigger plugin

TAF TE Trigger plugin allows the scheduling of test builds in TAF Test Executor environment. The Trigger plugin should be
installed on your FEM jenkins.

##Installation

**Note:** The installation of the plugin may be controlled by your CI Execution team.

1. Download the latest TAF Trigger plugin from Nexus - [release](https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/artifact/maven/redirect?r=releases&g=com.ericsson.oss&a=taf-trigger&v=RELEASE&p=hpi) or snapshot.
2. Go to Manage Jenkins -> Manage Plugins -> Advanced -> Upload Plugin, select the plugin .hpi file and upload it.
3. You can also copy the .hpi file to $JENKINS_HOME/plugins manually.
4. Restart Jenkins.

##Configuration

For configuration guidelines click [here.](te_trigger_plugin.html)

###TE Trigger Plugin Repo

TE Trigger Plugin can be found [here.](https://gerrit.ericsson.se/#/admin/projects/OSS/com.ericsson.oss/axis-jenkins-plugin)
