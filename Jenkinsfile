#!/usr/bin/env groovy
@Library('jenkins') _

node('sandbox') {

  repo = 'sample-apps'
  app = 'springboot-kafka-app'

  try {
    ws("workspace/${repo}") {

      stage_git()

      def gitDiff = sh script: "git diff --name-only master > gitdiff.txt", returnStdout: true
      echo "${gitDiff}"

      // def file = readFile location
      // def lines = file.split("\n").trim()
      // for (line in lines) {
      //     line = line.trim()
      //     echo "$line"
      // }

    }
  }
  catch (error) {
    echo "Error: ${error.toString()}"
    echo "ErrorStack: ${error.getStackTrace()}"
    currentBuild.result = 'Failure'
  }
}