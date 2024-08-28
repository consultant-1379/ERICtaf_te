<?xml version='1.0' encoding='UTF-8'?>
<com.ericsson.cifwk.taf.executor.TafExecutionProject plugin="te-jenkins-plugin@${pluginVersion}">
    <actions/>
    <description></description>
    <logRotator class="hudson.tasks.LogRotator">
        <daysToKeep>2</daysToKeep>
        <numToKeep>-1</numToKeep>
        <artifactDaysToKeep>-1</artifactDaysToKeep>
        <artifactNumToKeep>-1</artifactNumToKeep>
    </logRotator>
    <keepDependencies>false</keepDependencies>
    <properties/>
    <scm class="hudson.scm.NullSCM"/>
    <assignedNode>${agentLabel}</assignedNode>
    <canRoam>false</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers/>
    <concurrentBuild>true</concurrentBuild>
    <builders>
        <com.ericsson.cifwk.taf.executor.TafExecutionBuilder/>
        <com.ericsson.cifwk.taf.executor.TafManualTestExecutionBuilder/>
    </builders>
    <publishers/>
    <buildWrappers/>
</com.ericsson.cifwk.taf.executor.TafExecutionProject>