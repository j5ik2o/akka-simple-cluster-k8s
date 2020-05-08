# Simple akka cluster on kubernetes cluster

This repository demonstrates how to deploy akka cluster application on kubernetes. 
To try it locally on minikube:

```bash
$ sh minikube-deploy.sh

$ KUBE_IP=$(minikube ip)
$ MANAGEMENT_PORT=$(kubectl -n akka-simple-cluster-ns get svc akka-simple-cluster -ojsonpath="{.spec.ports[?(@.name==\"management\")].nodePort}")
$ curl http://$KUBE_IP:$MANAGEMENT_PORT/cluster/members | jq
$ API_PORT=$(kubectl -n akka-simple-cluster-ns get svc akka-simple-cluster -ojsonpath="{.spec.ports[?(@.name==\"api\")].nodePort}")
$ curl http://$KUBE_IP:$API_PORT/
<span>Wellcome to Sagrada Write API</span>
<span>Hello from SoftwareMill!</span>
$ curl -X POST http://$KUBE_IP:$API_PORT/counter/test
ok
$ curl http://$KUBE_IP:$API_PORT/counter/test
1
$ curl -X POST http://$KUBE_IP:$API_PORT/counter/test
ok
$ curl http://$KUBE_IP:$API_PORT/counter/test
2
```
