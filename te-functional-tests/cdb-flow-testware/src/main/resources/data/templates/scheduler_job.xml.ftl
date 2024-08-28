<?xml version='1.0' encoding='UTF-8'?>
<com.ericsson.cifwk.taf.executor.TafScheduleProject plugin="te-jenkins-plugin@${pluginVersion}">
    <actions/>
    <description></description>
    <logRotator class="hudson.tasks.LogRotator">
        <daysToKeep>2</daysToKeep>
        <numToKeep>-1</numToKeep>
        <artifactDaysToKeep>-1</artifactDaysToKeep>
        <artifactNumToKeep>-1</artifactNumToKeep>
    </logRotator>
    <keepDependencies>false</keepDependencies>
    <scm class="hudson.scm.NullSCM"/>
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <concurrentBuild>true</concurrentBuild>
    <builders>
        <com.ericsson.cifwk.taf.executor.TafScheduleBuilder/>
    </builders>
    <publishers/>
    <buildWrappers/>
    <reportMbHost>${reportingMbHost}</reportMbHost>
    <reportMbPort>${reportingMbPort}</reportMbPort>
    <reportMbUsername>${reportingMbUsername}</reportMbUsername>
    <reportMbPassword>${reportingMbPassword}</reportMbPassword>
    <reportMbExchange>${mbReportExchange}</reportMbExchange>
    <reportMbDomainId>${mbReportDomainId}</reportMbDomainId>
    <reportsHost>${reportsHost}</reportsHost>
    <localReportsStorage>${localReportsStorage}</localReportsStorage>
    <reportingScriptsFolder>${reportingScriptsFolder}</reportingScriptsFolder>
    <uploadToOssLogs>true</uploadToOssLogs>
    <minExecutorDiskSpaceGB>${minExecutorDiskSpaceGB}</minExecutorDiskSpaceGB>
    <minExecutorMemorySpaceGB>${minExecutorMemorySpaceGB}</minExecutorMemorySpaceGB>
    <allureVersion>${allureVersion}</allureVersion>
    <deletableFlowsAgeInDays>1</deletableFlowsAgeInDays>
    <runtimeLimitations>
        <maxThreadCount>1000</maxThreadCount>
    </runtimeLimitations>
</com.ericsson.cifwk.taf.executor.TafScheduleProject>
