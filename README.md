# Simple akka cluster on kubernetes cluster

This repository demonstrates how to deploy akka cluster application on kubernetes. 
To try it locally on minikube:

```bash
$ sh minikube-deploy.sh

$ KUBE_IP=$(minikube ip)
$ MANAGEMENT_PORT=$(kubectl get svc akka-simple-cluster -ojsonpath="{.spec.ports[?(@.name==\"management\")].nodePort}")
$ curl http://$KUBE_IP:$MANAGEMENT_PORT/cluster/members | jq
$ API_PORT=$(kubectl get svc akka-simple-cluster -ojsonpath="{.spec.ports[?(@.name==\"api\")].nodePort}")
$ curl http://$KUBE_IP:$API_PORT/
```
