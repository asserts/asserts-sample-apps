#!/bin/sh

set -o errexit

. kind-cleanup.sh

echo "Creating kind-kafka Cluster"
kind create cluster --name=kafka

docker build -t ai.asserts.springboot-kafka-app .

kind load docker-image ai.asserts.springboot-kafka-app --name=kafka

kubectl create ns springboot-kafka-app

cd ../../helm

helm dep up springboot-kafka-app

helm -n springboot-kafka-app install springboot-kafka-app ./springboot-kafka-app --set localdev.enabled=true

cd ../apps/springboot-kafka-app/

# Give Prometheus Pod time to spin up before trying to Port Froward it to localhost
sleep 30