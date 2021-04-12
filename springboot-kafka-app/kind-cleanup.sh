#!/bin/sh

set -o errexit

echo "Checking if kind-kafka cluster exists"
if [[ $(kind get clusters | grep kafka) ]]; then 
    echo "Deleting existing kind-kafka cluster"
    kind delete cluster --name=kafka
fi
