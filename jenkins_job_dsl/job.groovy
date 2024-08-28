/*
Full API documentation:
https://jenkinsci.github.io/job-dsl-plugin/

Job DSL playground:
http://job-dsl.herokuapp.com/
*/

// FEM119 environment variables
GERRIT_CENTRAL = '${GERRIT_CENTRAL}'
GERRIT_MIRROR = '${GERRIT_MIRROR}'
GERRIT_SERVER = 'gerrit.ericsson.se'
TE_TESTBED = 'atvts3471.athtem.eei.ericsson.se'

// Common properties
GIT_PROJECT = 'OSS/com.ericsson.cifwk/ERICtaf_te'
GIT_BRANCH = 'master'

// Jenkins slave labels
SLAVE_DOCKER = 'FEM119_POD_H_docker_build_slave'
SLAVE_RHEL = 'RHEL6.4_OGE'
SLAVE_TAF = 'taf_main_slave'
SLAVE_TAF_6 = 'taf_main_slave_6'
SLAVE_TEST_ENVIRONMENT = 'FEM119_POD_H_dockerised_TE_test_env'

// Job names
JOB_GERRIT_UNIT_TESTS = 'TE-AA-gerrit-unit-tests'
JOB_GERRIT_INTEGRATION_TESTS = 'TE-AB-gerrit-integration-tests'
JOB_UNIT_TESTS = 'TE-BA-unit-tests'
JOB_INTEGRATION_TESTS = 'TE-BB-integration-tests'

JOB_BUILD_DOCKER_IMAGES_ALL = 'TE-CA-docker-images-all'
JOB_BUILD_DOCKER_IMAGES_ONE = 'TE-CB-docker-images-one'
JOB_TEST_ENVIRONMENT = 'TE-CC-test-environment'
JOB_ACCEPTANCE_TESTS = 'TE-CD-acceptance-tests'
JOB_UPDATE_CHANGELOG = 'TE-CE-changelog'
JOB_UPDATE_USER_DOCS = 'TE-CF-update-docs'
JOB_BUILD_FLOW = 'TE-XX-build-flow'

JOB_RELEASE_NEXUS = 'TE-DA-release-nexus'
JOB_RELEASE_DOCKER_IMAGES_ALL = 'TE-DB-release-docker-images-all'
JOB_RELEASE_DOCKER_IMAGES_ONE = 'TE-DC-release-docker-images-one'
JOB_RELEASE_FLOW = 'TE-XX-release-flow'

// Job specific properties
SKIP_MAVEN_MODULES = '-pl "!te-metrics-dwh,!te-metrics-olap-schema,!te-puppet-scripts"'
MAVEN_GOALS_UNIT_TESTS = "-U clean source:jar deploy ${SKIP_MAVEN_MODULES}"
MAVEN_GOALS_GERRIT_UNIT_TESTS = "-U clean verify ${SKIP_MAVEN_MODULES}"
MAVEN_GOALS_INTEGRATION_TESTS = "-U clean test -Pitest,parallel -T 1 ${SKIP_MAVEN_MODULES}"

// Docker image maven profiles
DOCKER_IMAGE_PARAM = 'dockerImage'
DOCKER_IMAGE_MASTER = 'taf_te_master'
DOCKER_IMAGE_GRID_MASTER = 'taf_te_grid_master'
DOCKER_IMAGE_BASE_SLAVE = 'taf_te_base_slave'
DOCKER_IMAGE_SLAVE = 'taf_te_slave'
DOCKER_IMAGE_MESSAGE_BUS = 'taf_te_message_bus'
DOCKER_IMAGE_LDAP = 'taf_ldap'
DOCKER_IMAGE_AAT = 'taf_aat'

createGerritJob(JOB_GERRIT_UNIT_TESTS) {
    customDescription(delegate, 'Runs TE Unit tests as a part of Gerrit verification')
    steps {
        mavenGoals(delegate, MAVEN_GOALS_GERRIT_UNIT_TESTS)
    }
}

createGerritJob(JOB_GERRIT_INTEGRATION_TESTS) {
    customDescription(delegate, 'Runs TE Integration tests as a part of Gerrit verification')
    localBlockOn(delegate, JOB_INTEGRATION_TESTS)
    steps {
        mavenGoals(delegate, MAVEN_GOALS_INTEGRATION_TESTS)
    }
}

createSimpleJob(JOB_UNIT_TESTS) {
    customDescription(delegate, 'Runs TE Unit tests')
    label(SLAVE_TAF)
    steps {
        mavenGoals(delegate, MAVEN_GOALS_UNIT_TESTS)
    }
    publishers {
        mailer('kirill.shepitko@ericsson.com, john.lynch@ericsson.com', false, true)
    }
}

createSimpleJob(JOB_INTEGRATION_TESTS) {
    customDescription(delegate, 'Runs TE Integration tests')
    localBlockOn(delegate, JOB_GERRIT_INTEGRATION_TESTS)
    label(SLAVE_RHEL)
    steps {
        mavenGoals(delegate, MAVEN_GOALS_INTEGRATION_TESTS)
    }
}

createBuildFlowJob(JOB_BUILD_DOCKER_IMAGES_ALL) {
    customDescription(delegate, 'Builds all TE Docker images and pushes them to the Docker registry')
    globalBlockOn(delegate, JOB_RELEASE_DOCKER_IMAGES_ALL)
    createTeDockerImagesBuildFlow(delegate, JOB_BUILD_DOCKER_IMAGES_ONE)
}

createSimpleJob(JOB_BUILD_DOCKER_IMAGES_ONE) {
    customDescription(delegate, "Builds TE Docker image specified by '$DOCKER_IMAGE_PARAM' and pushes it to the Docker registry")
    globalBlockOn(delegate, JOB_RELEASE_DOCKER_IMAGES_ONE)
    parameters {
        choiceParam(DOCKER_IMAGE_PARAM,
                [DOCKER_IMAGE_LDAP,
                 DOCKER_IMAGE_AAT,
                 DOCKER_IMAGE_MASTER,
                 DOCKER_IMAGE_GRID_MASTER,
                 DOCKER_IMAGE_BASE_SLAVE,
                 DOCKER_IMAGE_SLAVE,
                 DOCKER_IMAGE_MESSAGE_BUS],
                'TE Docker image')
    }
    concurrentBuild()
    jdk('(System)')
    label(SLAVE_DOCKER)
    wrappers {
        buildName('#${BUILD_NUMBER} ${ENV,var="dockerImage"}')
    }
    steps {
        groovyCommand('''\
            //Grab the project version from the pom file and pass it to the maven step
            def file = new File("pom.xml")
            def doc = new XmlSlurper().parse(file)
            def version = doc.version.text()
            
            def line = "PROJ_VERSION=$version"
            
            def outFile = new File("version.props")
            outFile.write(line)
            '''.stripIndent())
        environmentVariables {
            propertiesFile('version.props')
        }
        mavenGoals(delegate, """\
            -X -B -U 
            -DARM_REPOSITORY=snapshots 
            -DTE_PLUGIN_ARTIFACT_VERSION=\${PROJ_VERSION} 
            -DTE_SLAVE_ARTIFACT_VERSION=\${PROJ_VERSION} 
            -DpushImage -pl te-docker -DdockerImage=\${$DOCKER_IMAGE_PARAM}
            clean deploy
            """.stripIndent())
    }
}

createSimpleJob(JOB_TEST_ENVIRONMENT) {
    customDescription(delegate, 'Installs TE as Docker containers on the test environment')
    globalBlockOn(delegate, JOB_ACCEPTANCE_TESTS)
    jdk('(System)')
    label(SLAVE_TEST_ENVIRONMENT)
    steps {
        mavenGoals(delegate, '-U -pl te-docker clean compile')
        shell('''\
            docker stop $(docker ps -a -q) || echo 'docker stop failed to execute'
            docker rm $(docker ps -a -q) || echo 'docker rm failed to execute'
            docker rmi $(docker images | awk '$2 ~ /SNAPSHOT/ { print $3 }') || echo 'docker rmi failed to execute'
            cd ${WORKSPACE}/te-docker/target/classes/testbed
            bash te-testbed.sh
            '''.stripIndent())
    }
}

createSimpleJob(JOB_ACCEPTANCE_TESTS) {
    customDescription(delegate, 'Runs TE Acceptance tests')
    globalBlockOn(delegate, JOB_TEST_ENVIRONMENT)
    jdk('JDK 1.8 Docker Slave')
    label(SLAVE_TEST_ENVIRONMENT)
    quietPeriod(30)
    parameters {
        stringParam('TE_TESTBED', TE_TESTBED)
    }
    scm {
        git {
            remote {
                name('gm')
                url("${GERRIT_MIRROR}/${GIT_PROJECT}")
            }
            remote {
                name('gc')
                url("${GERRIT_CENTRAL}/${GIT_PROJECT}")
            }
            branch(GIT_BRANCH)
            extensions {
                cleanAfterCheckout()
            }
            configure {
                it << gitTool('vApp Git')
            }
        }
    }
    steps {
        maven {
            mavenInstallation('Maven 3.3.9')
            goals('-B -U clean install -Pacceptance -Dmaven.test.failure.ignore=false -Dtaf.profiles=grid,test ' +
                    '-Dhost.te_master.ip=$TE_TESTBED -Dhost.reporting_message_bus.ip=$TE_TESTBED -Dhost.event_repository.ip=$TE_TESTBED')
            rootPOM('te-functional-tests/pom.xml')
            mavenOpts('-XX:MaxPermSize=512m')
        }
    }
    publishers {
        allureReportPublisher {
            config {
                jdk('InheritFromJob')
                commandline('')
                resultsPattern('target/allure-results')
                properties {
                    propertyConfig {
                        key('allure.issues.tracker.pattern')
                        value('https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/%s')
                    }
                    propertyConfig {
                        key('allure.issues.tracker.pattern')
                        value('http://jira-oss.lmera.ericsson.se/browse/%s')
                    }
                }
                reportBuildPolicy('ALWAYS')
                includeProperties(true)
            }
        }
    }
    configure {
        def testng = it / publishers / 'hudson.plugins.testng.Publisher'
        testng << reportFilenamePattern('test-output/testng-results.xml')
        testng << escapeTestDescp(true)
        testng << escapeExceptionMsg(true)
    }
}

createBuildFlowJob(JOB_BUILD_FLOW) {
    customDescription(delegate, "Runs TE build flow when branch '${GIT_BRANCH}' is changed")
    globalBlockOn(delegate, JOB_RELEASE_FLOW)
    createGerritTrigger(delegate) {
        refUpdated()
    }
    buildFlow("""\
        parallel (
            { build '$JOB_UNIT_TESTS' },
            { build '$JOB_INTEGRATION_TESTS' }
        )
        build '$JOB_BUILD_DOCKER_IMAGES_ALL'
        build '$JOB_TEST_ENVIRONMENT'
        build '$JOB_ACCEPTANCE_TESTS'
        build '$JOB_UPDATE_CHANGELOG'
        build ('$JOB_UPDATE_USER_DOCS', Version: "snapshot")
        """.stripIndent())
}

mavenJob(JOB_RELEASE_NEXUS) {
    customDescription(delegate, 'Job to release TAF Executor to nexus')
    globalBlockOn(delegate, JOB_BUILD_FLOW)
    logRotator {
        daysToKeep(21)
        numToKeep(10)
    }
    jdk('JDK 1.8.0_25')
    label(SLAVE_RHEL)
    scm {
        git {
            remote {
                name('gm')
                url("${GERRIT_MIRROR}/${GIT_PROJECT}")
            }
            remote {
                name('gc')
                url("${GERRIT_CENTRAL}/${GIT_PROJECT}")
            }
            branch(GIT_BRANCH)
            extensions {
                perBuildTag()
                cleanAfterCheckout()
                disableRemotePoll()
            }
            configure {
                def ext = it / 'extensions'
                def pkg = 'hudson.plugins.git.extensions.impl'
                ext / "${pkg}.UserExclusion" << excludedUsers('Jenkins Release')
                ext / "${pkg}.UserIdentity" << name('Jenkins Release')
            }
        }
    }
    preBuildSteps {
        shell("""\
            export GIT_URL=\${GIT_URL_1}
            
            #cannot push back to gerrit mirror so need to set url to GC
            repo=\$(echo \$GIT_URL | sed 's#.*OSS/##g')
            
            git remote set-url --push gc \${GERRIT_CENTRAL}/OSS/\${repo}
            
            #run script to check gerrit mirror sync
            /proj/litpadm200/data/scripts/check_gerrit_sync.sh
            
            git checkout ${GIT_BRANCH} || git checkout -b ${GIT_BRANCH}
            git reset --hard gm/${GIT_BRANCH}
            """.stripIndent())
    }
    mavenInstallation('Maven 3.3.9')
    goals("""
        -V -Dresume=false -DlocalCheckout=true ${SKIP_MAVEN_MODULES} 
        release:prepare -DpreparationGoals="clean install -DskipTests -Prpm" 
        release:perform -Dgoals="clean deploy -DskipTests -Prpm" 
        """.stripIndent())
    mavenOpts('-XX:MaxPermSize=1024m')
    configure {
        it / 'runPostStepsIfResult' << name('SUCCESS')
    }
    publishers {
        git {
            pushOnlyIfSuccess()
            branch('gc', GIT_BRANCH)
        }
        allowBrokenBuildClaiming()
    }
}

createBuildFlowJob(JOB_RELEASE_DOCKER_IMAGES_ALL) {
    customDescription(delegate, 'Release all TE Docker images to the Docker registry')
    globalBlockOn(delegate, JOB_BUILD_DOCKER_IMAGES_ALL)
    createTeDockerImagesBuildFlow(delegate, JOB_RELEASE_DOCKER_IMAGES_ONE)
}

createSimpleJob(JOB_RELEASE_DOCKER_IMAGES_ONE) {
    customDescription(delegate, "Releases TE Docker image specified by '$DOCKER_IMAGE_PARAM' to the Docker registry")
    globalBlockOn(delegate, JOB_BUILD_DOCKER_IMAGES_ONE)
    parameters {
        choiceParam(DOCKER_IMAGE_PARAM,
                [DOCKER_IMAGE_LDAP,
                 DOCKER_IMAGE_AAT,
                 DOCKER_IMAGE_MASTER,
                 DOCKER_IMAGE_GRID_MASTER,
                 DOCKER_IMAGE_BASE_SLAVE,
                 DOCKER_IMAGE_SLAVE,
                 DOCKER_IMAGE_MESSAGE_BUS],
                'TE Docker image')
    }
    concurrentBuild()
    jdk('(System)')
    label(SLAVE_DOCKER)
    steps {
        shell('''\
            # Need to switch to the latest tag, created by Maven release plugin, to get the release code
            
            # Get new tags from the remote
            git fetch --tags
             
            # Get the latest tag name
            latestTag=$(git describe --tags `git rev-list --tags --max-count=1`)
             
            # Checkout the latest tag
            git checkout $latestTag
            '''.stripIndent())
        groovyCommand('''\
            //Grab the project version from the pom file and pass it to the maven step
            def file = new File("pom.xml")
            def doc = new XmlSlurper().parse(file)
            def version = doc.version.text()
            
            def line = "PROJ_VERSION=$version"
            
            def outFile = new File("version.props")
            outFile.write(line)
            '''.stripIndent())
        environmentVariables {
            propertiesFile('version.props')
        }
        mavenGoals(delegate, """\
            -U -pl te-docker
			-DTE_PLUGIN_ARTIFACT_VERSION=\${PROJ_VERSION} 
            -DTE_SLAVE_ARTIFACT_VERSION=\${PROJ_VERSION} 
            clean package -DpushImageTag 
            -DdockerImage=\${$DOCKER_IMAGE_PARAM} 
            """.stripIndent())
    }
}

createBuildFlowJob(JOB_RELEASE_FLOW) {
    customDescription(delegate, 'Runs TE release flow')
    globalBlockOn(delegate, JOB_BUILD_FLOW)
    buildFlow("""\
        build '$JOB_RELEASE_NEXUS'
        build '$JOB_RELEASE_DOCKER_IMAGES_ALL'     
        build ('$JOB_UPDATE_USER_DOCS', Version: "latest")   
        """.stripIndent())
}

createSimpleJob(JOB_UPDATE_CHANGELOG) {
    customDescription(delegate, 'Creates the changelog html page and uploads to taflanding')
    jdk('(System)')
    label(SLAVE_TAF_6)
    steps {
        mavenGoals(delegate, 'com.ericsson.cifwk.taf:tafchangelog-maven-plugin:1.0.14:generate -X -e')
        shell('''\
            cd /proj/eiffel004_config_fem119/slaves/workspace-slave6/workspace/TE-CE-changelog/target
            targetDir=/proj/PDU_OSS_CI_TAF/taflanding/tereleases
            rm -f ${targetDir}/changelog.html
            cp changelog.html ${targetDir}/changelog.html
            '''.stripIndent())
    }
}

createSimpleJob(JOB_UPDATE_USER_DOCS) {
    customDescription(delegate, 'Publishes user docs to http://taf.lmera.ericsson.se/taflanding/tedocs/snapshot/ or ' +
            'http://taf.lmera.ericsson.se/taflanding/tedocs/latest/ depending on parameter specified')
    jdk('JDK 1.7.0_21')
    label(SLAVE_RHEL)
    parameters {
        choiceParam('Version',['snapshot','latest'], 'Where to deploy docs to')
        stringParam('BaseDocsDir','/proj/PDU_OSS_CI_TAF/taflanding/tedocs','')
    }
    scm {
        git {
            remote {
                name('gm')
                url("${GERRIT_MIRROR}/${GIT_PROJECT}")
            }
            remote {
                name('gc')
                url("${GERRIT_CENTRAL}/${GIT_PROJECT}")
            }
            branch(GIT_BRANCH)
            extensions {
                cleanBeforeCheckout()
            }
            configure {
                it << gitTool('GIT 1.8.4.2')
            }
        }
    }
    steps {
        shell('''\
            export targetDir="${BaseDocsDir}/${Version}/"
            if [ ! -d "$targetDir" ]; then
            echo "$targetDir not present, it will be created"
            mkdir -m u+w $targetDir
            fi
            '''.stripIndent())
        maven {
            mavenInstallation('Maven 3.0.5')
            goals('-V clean install -DskipTests site site:stage -DstagingDirectory=${BaseDocsDir}/${Version}/')
            rootPOM('te-docs/pom.xml')
            mavenOpts('-Xmx8g')
            mavenOpts('-XX:MaxPermSize=4g')
        }
    }
}

def mavenGoals(Object context, String... mavenGoals) {
    executeInContext(context) {
        mavenGoals.each { goal ->
            maven {
                mavenInstallation('Maven 3.3.9')
                goals("${goal}")
                mavenOpts('-Xms256m')
                mavenOpts('-Xmx512m')
            }
        }
    }
}

def createGerritJob(String jobName, Closure jobClosure = {}) {
    createJob(jobName) {
        createScm(delegate, '$GERRIT_BRANCH', '$GERRIT_REFSPEC') {
            extensions {
                choosingStrategy {
                    gerritTrigger()
                }
            }
        }
        concurrentBuild()
        label(SLAVE_TAF)
        createGerritTrigger(delegate) {
            patchsetCreated()
        }
        executeInContext(delegate, jobClosure)
    }
}

def createSimpleJob(String jobName, Closure jobClosure = {}) {
    createJob(jobName) {
        createScm(delegate, GIT_BRANCH)
        executeInContext(delegate, jobClosure)
    }
}

def createJob(String jobName, Closure jobClosure = {}) {
    job(jobName) {
        customDescription(delegate)
        logRotator {
            daysToKeep(21)
            numToKeep(10)
        }
        jdk('JDK 1.8.0_25')
        wrappers {
            colorizeOutput()
            timestamps()
        }
        publishers {
            allowBrokenBuildClaiming()
        }
        executeInContext(delegate, jobClosure)
    }
}

def createBuildFlowJob(String jobName, Closure jobClosure = {}) {
    buildFlowJob(jobName) {
        logRotator {
            daysToKeep(21)
            numToKeep(10)
        }
        jdk('(System)')
        label(SLAVE_RHEL)
        wrappers {
            colorizeOutput()
            timestamps()
        }
        publishers {
            allowBrokenBuildClaiming()
        }
        executeInContext(delegate, jobClosure)
    }
}

def createScm(context, String aBranch = GIT_BRANCH, String aRefspec = '', Closure scmClosure = {}) {
    context.scm {
        git {
            remote {
                name('origin')
                url("${GERRIT_CENTRAL}/${GIT_PROJECT}")
                refspec(aRefspec)
            }
            branch(aBranch)
            extensions {
                cleanBeforeCheckout()
            }
            executeInContext(delegate, scmClosure)
        }
    }
}

def createGerritTrigger(context, Closure eventsClosure = {}) {
    context.triggers {
        gerrit {
            events {
                executeInContext(delegate, eventsClosure)
            }
            project(GIT_PROJECT, GIT_BRANCH)
            configure {
                it << serverName(GERRIT_SERVER)
            }
        }
    }
}

def createTeDockerImagesBuildFlow(context, String dockerImageJob) {
    context.buildFlow("""\
        parallel (
            { build ('$dockerImageJob', $DOCKER_IMAGE_PARAM : '$DOCKER_IMAGE_LDAP') },
            { build ('$dockerImageJob', $DOCKER_IMAGE_PARAM : '$DOCKER_IMAGE_AAT') },
            {
                build ('$dockerImageJob', $DOCKER_IMAGE_PARAM : '$DOCKER_IMAGE_MASTER')
                build ('$dockerImageJob', $DOCKER_IMAGE_PARAM : '$DOCKER_IMAGE_GRID_MASTER')
            },
            {
                build ('$dockerImageJob', $DOCKER_IMAGE_PARAM : '$DOCKER_IMAGE_BASE_SLAVE')
                build ('$dockerImageJob', $DOCKER_IMAGE_PARAM : '$DOCKER_IMAGE_SLAVE')
            },
            { build ('$dockerImageJob', $DOCKER_IMAGE_PARAM : '$DOCKER_IMAGE_MESSAGE_BUS') }
        )
        """.stripIndent())
}

def customDescription(context, String desc = null) {
    String suffix = 'DO NOT MODIFY IT! THIS JOB IS AUTO-GENERATED.'
    context.description(desc ? "$desc\n\n$suffix" : suffix)
}

def globalBlockOn(context, String jobName) {
    context.blockOn(jobName) {
        blockLevel('GLOBAL')
    }
}

def localBlockOn(context, String jobName) {
    context.blockOn(jobName) {
        blockLevel('NODE')
    }
}

def executeInContext(context, Closure closure) {
    closure.delegate = context
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call()
}
