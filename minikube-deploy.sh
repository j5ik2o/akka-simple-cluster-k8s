#!/bin/sh

minikube delete
rm -fr ~/.minikube

minikube start --vm-driver hyperkit --cpus 6 --memory 4096 --disk-size 60g
eval $(minikube docker-env)
sbt docker:publishLocal

kubectl create namespace akka-simple-cluster-ns
kubectl create serviceaccount akka-simple-cluster-ns

kubectl create -f k8s/simple-akka-cluster-rbac.yml --namespace akka-simple-cluster-ns
kubectl create -f k8s/simple-akka-cluster-deployment.yml --namespace akka-simple-cluster-ns
kubectl create -f k8s/simple-akka-cluster-service.yml --namespace akka-simple-cluster-ns
