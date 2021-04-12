#!/bin/sh

set -o errexit

echo "Checking if kind-karte cluster exists"
if [[ $(kind get clusters | grep karte) ]]; then 
    echo "Deleting existing kind-karte cluster"
    kind delete cluster --name=karte
fi
