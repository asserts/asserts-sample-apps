#!/bin/sh

set -o errexit

echo "Checking if kind-mysqlapp cluster exists"
if [[ $(kind get clusters | grep mysqlapp) ]]; then
    echo "Deleting existing kind-mysqlapp cluster"
    kind delete cluster --name=mysqlapp
fi
