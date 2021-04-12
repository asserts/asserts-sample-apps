#!/bin/sh

# set -o errexit
# set -o nounset

. kind-cleanup.sh

echo "Creating Kind Cluster"
kind create cluster --name=karte
# kind create cluster  --config kind-multi-node-config.yaml

echo "Building Karte docker image locally"
docker build -t ai.asserts.karte .

echo "Loading Karte image into your kind cluster"
kind load docker-image ai.asserts.karte --name=karte

echo "Deploying the Istio operator"
istioctl operator init

echo "Creating namespaces in Kind"
kubectl create ns istio-system
kubectl create ns prometheus

cd helm
echo "Install Istio"
kubectl apply -f karte/istio-operator.yaml

echo "Update Karte’s dependencies with Helm"
helm dep up karte

echo "Install Karte Helm Chart"
helm install karte --generate-name