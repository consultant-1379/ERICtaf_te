<#assign esc_d = '$'>
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- groupId must be the same as the testware.groupId (or a subset of it) -->
    <!-- for more info see: taf-maven-plugin -->
    <groupId>com.ericsson.cifwk</groupId>
    <artifactId>manual-test-pom</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <build>
        <plugins>
        <#if (additionalMavenPlugins??)>${additionalMavenPlugins}</#if>
            <plugin>
                <groupId>com.ericsson.cifwk.taf</groupId>
                <artifactId>taf-allure-manual-maven-plugin</artifactId>
                <version>${manual_test_plugin_version}</version>
                <configuration>
                    <testPlanIds>${testPlanIdsCsv}</testPlanIds>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>test</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        <#include "allure_site_deploy_include.ftl"/>
        </plugins>
    </build>

<#include "repositories_include.ftl"/>

</project>
