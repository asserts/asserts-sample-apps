#!/bin/sh

set -o errexit

echo "Checking if Kind cluster already exists"
if [[ $(kind get clusters | grep kafka) ]]; then 
    echo "Deleting existing kind-kafka cluster"
    kind delete cluster --name=kafka
fi

echo "Creating kind-kafka Cluster"
kind create cluster --name=kafka

rm -Rf tmp

./gradlew clean processResources bootJar

mkdir tmp
cp build/libs/* tmp
cp build/resources/main/*.yml tmp
cp build/resources/main/*.properties tmp

docker build -t ai.asserts.springboot-kafka-app .

kind load docker-image ai.asserts.springboot-kafka-app --name=kafka

kubectl create ns springboot-kafka-app

cd ../../helm

helm dep up springboot-kafka-app

helm -n springboot-kafka-app install springboot-kafka-app ./springboot-kafka-app --set localdev.enabled=true

cd ../apps/springboot-kafka-app/

# Give Prometheus Pod time to spin up before trying to Port Froward it to localhost
sleep 30