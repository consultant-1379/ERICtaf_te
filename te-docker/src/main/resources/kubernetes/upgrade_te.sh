#!/bin/bash

# Delete existing agent Pods
for each in $( kubectl get pods -n taf-te -o jsonpath='{.items[*].metadata.name}' | tr -s '[[:space:]]' '\n' |  grep "te-slave");
do
  kubectl delete pods -n taf-te $each
done

# TODO: pass image version as a parameter to script
kubectl -n taf-te set image deployment/te-jenkins-deployment te-master-container=armdocker.rnd.ericsson.se/proj_taf_te/taf_te_grid_master:2.4

## Useful commands:

# Check out the deployment status
#kubectl -n taf-te rollout status deployments te-jenkins-deployment

# Review deployment history
#kubectl -n taf-te rollout history deployment/te-jenkins-deployment

# Roll back the deployment to previous version
#kubectl -n taf-te rollout undo deployment/te-jenkins-deployment