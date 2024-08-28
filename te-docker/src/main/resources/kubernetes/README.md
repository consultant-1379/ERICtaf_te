# Setting up a TE Grid in Kubernetes cluster

Prerequisite: K8s cluster with 1+ minions.

## Create a K8s Secret for image pulls from Ericsson Docker registry

**NOTE**: replace `REPLACE_THIS` with the real password before executing:

`kubectl create secret docker-registry armdocker.rnd.ericsson.se --namespace=taf-te --docker-server=armdocker.rnd.ericsson.se --docker-username=tafuser --docker-password=REPLACE_THIS --docker-email=no-reply@ericsson.com`

## Create the necessary Kubernetes entities

This is done using the YAML manifests from this folder. They are ordered in the sequence of execution.

* `01_te_ns.yaml` - Namespace for TE master and agents (ex "slaves").
* `02_te_mb_rc.yml` - Internal message bus replication controller.
* `03_te_mb_service.yml` - Service to access internal message bus.
* `04_allure_ms_rc.yml` - Allure microservice replication controller.
* `05_allure_ms_service.yml` - Service to access Allure MS.
* `06_te_grid_master_deployment.yml` - TE Grid Jenkins (master) deployment descriptor. Ensures the master's high availability.
Spawns slaves on demand by utilising K8s plugin. Works with Allure service. Uses volumes defined above.
* `07_te_master_service.yml` - Service to access TE Jenkins (master).

If it's a first installation, run all scripts above (`kubectl create -f <YAML file>`).

For upgrade, run only one of `02`, `04` and `06` scripts (`kubectl replace -f <YAML file>`).
Services with node ports and namespaces are not replaceable, so have to delete them first (`kubectl delete -f <YAML file>`) if needed.

TODO: change MB replication controller to Deployment for easier upgrade.

## Set up ingress for Jenkins service

As the PDU NAM Openstack does not have a load balancing function yet it is necessary to set up ingress for the Jenkins service.
This can be done using `kubectl create -f <YAML file>` on each of the below.

* `https://raw.githubusercontent.com/kubernetes/ingress/master/examples/deployment/nginx/default-backend.yaml`- set up the default nginx backend
* `https://raw.githubusercontent.com/kubernetes/ingress/master/examples/daemonset/nginx/nginx-ingress-daemonset.yaml` - set up the nginx ingress daemon set.
* `08_te_ingress.yml` - ingress for the jenkins service.
