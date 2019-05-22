#!/bin/sh

minikube start --vm-driver hyperkit --cpus 6 --memory 4096 --disk-size 60g
eval $(minikube docker-env)
sbt docker:publishLocal
kubectl create -f k8s/simple-akka-cluster-rbac.yml
kubectl create -f k8s/simple-akka-cluster-deployment.yml
kubectl create -f k8s/simple-akka-cluster-service.yml
