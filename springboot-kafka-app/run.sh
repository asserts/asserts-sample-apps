#!/bin/sh

set -o errexit

./gradlew clean processResources bootJar

mkdir tmp
cp build/libs/* tmp
cp build/resources/main/*.yml tmp
cp build/resources/main/*.properties tmp

docker-compose build
docker-compose up -d
rm -Rf tmp