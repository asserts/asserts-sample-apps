#!/bin/sh

set -o errexit

. kind-cleanup.sh

echo "Creating kind-mysqlapp Cluster"
kind create cluster --name=mysqlapp

docker build -t ai.asserts.mysql-app .

kind load docker-image ai.asserts.mysql-app --name=mysqlapp

kubectl create ns mysql-app

cd ../../helm

helm dep up mysql-app

helm -n mysql-app install mysql-app ./mysql-app --set localdev.enabled=true

cd ../apps/mysql-app/

# Give Prometheus Pod time to spin up before trying to Port Froward it to localhost
sleep 30