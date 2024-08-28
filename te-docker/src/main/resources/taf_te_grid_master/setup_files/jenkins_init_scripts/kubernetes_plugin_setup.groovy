package taf_te_grid_master.setup_files.jenkins_init_scripts

import com.ericsson.cifwk.taf.executor.TAFExecutor
import groovy.transform.Field
import jenkins.model.Jenkins
import org.csanchez.jenkins.plugins.kubernetes.ContainerTemplate
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud
import org.csanchez.jenkins.plugins.kubernetes.PodImagePullSecret
import org.csanchez.jenkins.plugins.kubernetes.PodTemplate

@Field
String CLOUD_NAME = 'TE Grid Kubernetes cloud'

@Field
String SLAVE_IMAGE_NAME = 'armdocker.rnd.ericsson.se/proj_taf_te/taf_te_base_slave:$SETUP__GRID_TE_SLAVE_VERSION'

// 'taf' label is auto-added on itself by TE slave, but if we omit it here, slave won't be created on demand
@Field
String SLAVE_LABEL = TAFExecutor.TAF_NODE_LABEL + ' te_grid_slave'

@Field
String SLAVE_NAMESPACE = 'taf-te'

@Field
int MAX_SLAVES = 10

@Field
int SLAVE_CONNECT_TIMEOUT = 100

@Field
int IDLE_MINUTES = 10

// Returns the DNS address to be used by slave
@Field
String JENKINS_DNS = "te-master"

@Field
String JENKINS_URL = "http://${JENKINS_DNS}/jenkins"

@Field
String K8S_SERVER_URL = '$SETUP__K8S_SERVER_URL';

Jenkins jenkins = Jenkins.instance
if(!jenkins.clouds.getByName(CLOUD_NAME)) {
    println '*** Setting up Kubernetes plugin to provide slaves on demand'
    Jenkins.instance.clouds.add(kubernetesCloudInstance())
} else {
    println '*** Kubernetes plugin already set up to provide TE slaves'
}

def kubernetesCloudInstance() {
    KubernetesCloud kubernetesCloud = new KubernetesCloud(CLOUD_NAME)
    kubernetesCloud.setServerUrl(K8S_SERVER_URL)
    kubernetesCloud.setSkipTlsVerify(true)
    kubernetesCloud.setNamespace(SLAVE_NAMESPACE)
    kubernetesCloud.setJenkinsUrl(JENKINS_URL)
    kubernetesCloud.setJenkinsTunnel("${JENKINS_DNS}:50000")
    kubernetesCloud.setContainerCapStr("${MAX_SLAVES}")
    kubernetesCloud.setRetentionTimeout(IDLE_MINUTES)
    kubernetesCloud.addTemplate(podTemplate())
    return kubernetesCloud
}

def podTemplate() {
    PodTemplate podTemplate = new PodTemplate()
    podTemplate.setName("te-slave-template")
    podTemplate.setInstanceCap(MAX_SLAVES)
    podTemplate.setContainers([containerTemplate()])
    podTemplate.setSlaveConnectTimeout(SLAVE_CONNECT_TIMEOUT)
    podTemplate.setIdleMinutes(IDLE_MINUTES)
    podTemplate.setLabel(SLAVE_LABEL)
    podTemplate.setImagePullSecrets(imagePullSecrets())
    return podTemplate
}

def containerTemplate() {
    def containerTemplate = new ContainerTemplate(SLAVE_IMAGE_NAME)
    containerTemplate.setName("jnlp")
    containerTemplate.setAlwaysPullImage(false)
//    containerTemplate.setSlaveImage(true)
//    containerTemplate.setSelfRegisteringSlave(true)
    containerTemplate.setCommand("")
    containerTemplate.setArgs("--te-master-address=${JENKINS_URL}")
    return containerTemplate
}

static List<PodImagePullSecret> imagePullSecrets() {
    // Secret has to be manually added to K8s first under the name of 'armdocker.rnd.ericsson.se'
    // - see https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/
    [new PodImagePullSecret("armdocker.rnd.ericsson.se")]
}