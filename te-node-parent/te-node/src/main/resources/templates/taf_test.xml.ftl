<#assign esc_d = '$'>
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- groupId must be the same as the testware.groupId (or a subset of it) -->
    <!-- for more info see: taf-maven-plugin -->
    <groupId>${testware.groupId}</groupId>
    <artifactId>test-pom</artifactId>
    <version>${testware.version}</version>
    <packaging>pom</packaging>

    <properties>
    <#if (taf_version??)>
        <tafversion>${taf_version}</tafversion>
    </#if>
    </properties>

    <dependencyManagement>
        <dependencies>
        <#if (taf_version??)>
            <dependency>
                <groupId>com.ericsson.cifwk</groupId>
                <artifactId>taf-bom</artifactId>
                <version>${esc_d}{tafversion}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </#if>
        <#if (user_defined_boms??)>

            <!-- user defined bom gavs -->
            <#list user_defined_boms as bom>
            <dependency>
                <groupId>${bom.groupId}</groupId>
                <artifactId>${bom.artifactId}</artifactId>
                <version>${bom.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            </#list>
        </#if>
        <#if (user_defined_poms??)>

            <!-- user defined pom gavs -->
            <#list user_defined_poms as user_poms>
            <dependency>
                <groupId>${user_poms.groupId}</groupId>
                <artifactId>${user_poms.artifactId}</artifactId>
                <version>${user_poms.version}</version>
            </dependency>
            </#list>
        </#if>

            <dependency>
                <groupId>${testware.groupId}</groupId>
                <artifactId>${testware.artifactId}</artifactId>
                <version>${testware.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        <#if (allure_version??)>
            <dependency>
                <groupId>ru.yandex.qatools.allure</groupId>
                <artifactId>allure-commons</artifactId>
                <version>${allure_version}</version>
            </dependency>
            <dependency>
                <groupId>ru.yandex.qatools.allure</groupId>
                <artifactId>allure-java-adaptor-api</artifactId>
                <version>${allure_version}</version>
            </dependency>
            <dependency>
                <groupId>ru.yandex.qatools.allure</groupId>
                <artifactId>allure-model</artifactId>
                <version>${allure_version}</version>
            </dependency>
        </#if>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>${testware.groupId}</groupId>
            <artifactId>${testware.artifactId}</artifactId>
            <version>${testware.version}</version>
        </dependency>
        <dependency>
            <groupId>com.ericsson.cifwk</groupId>
            <artifactId>testing-events-listener</artifactId>
        </dependency>
        <dependency>
            <groupId>com.ericsson.cifwk.taf.executor</groupId>
            <artifactId>te-taf-plugin</artifactId>
            <version>2.14</version>
        </dependency>
    <#if (additionalDependencies??)>
        <#list additionalDependencies as additionalDependency>
            <dependency>
                <groupId>${additionalDependency.groupId}</groupId>
                <artifactId>${additionalDependency.artifactId}</artifactId>
                <version>${additionalDependency.version}</version>
            </dependency>
        </#list>
    </#if>
    </dependencies>

    <build>
        <extensions>
            <extension>
                <groupId>org.apache.maven.wagon</groupId>
                <artifactId>wagon-http</artifactId>
                <version>2.12</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>com.ericsson.cifwk.taf</groupId>
                <artifactId>taf-maven-plugin</artifactId>
            <#if (taf_maven_plugin_version??)>
                <version>${taf_maven_plugin_version}</version>
            <#elseif (taf_version??)>
                <version>${esc_d}{tafversion}</version>
            </#if>
                <executions>
                    <execution>
                        <goals>
                            <goal>test</goal>
                        </goals>
                        <configuration>
                            <useSurefire>true</useSurefire>
                            <mavenSurefirePluginVersion>2.18.1</mavenSurefirePluginVersion>
                            <tafSurefireProviderVersion>${taf_surefire_provider_version}</tafSurefireProviderVersion>
                            <forkCount>0</forkCount>
                        <#if (suites??)>
                            <suites>${suites}</suites></#if>
                        <#if (groups?? )>
                            <groups>${groups}</groups></#if>
                        <#if (properties??)>
                        <properties>
                            <#list properties as property>
                                <#if (property.value??)><${property.name}>${property.value}</${property.name}></#if>
                            </#list>
                            </properties>
                        </#if>
                            <unpackSeleniumDrivers>false</unpackSeleniumDrivers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        <#include "allure_site_deploy_include.ftl"/>
        </plugins>
    </build>
<#include "repositories_include.ftl"/>
</project>
