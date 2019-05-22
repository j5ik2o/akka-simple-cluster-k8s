#!/bin/sh

minikube start --vm-driver hyperkit --cpus 6 --memory 4096 --disk-size 60g
eval $(minikube docker-env)
sbt docker:publishLocal
# create serviceAccount and role
kubectl create -f k8s/simple-akka-cluster-rbac.yml
# create deployment
kubectl create -f k8s/simple-akka-cluster-deployment.yml
# create service
kubectl create -f k8s/simple-akka-cluster-service.yml
