#!/bin/sh

set -o errexit

rm -Rf tmp

./gradlew clean processResources bootJar

mkdir tmp
cp build/libs/* tmp
cp build/resources/main/*.yml tmp
cp build/resources/main/*.properties tmp

docker-compose up --build -d
rm -Rf tmp