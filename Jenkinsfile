#!/usr/bin/env groovy
@Library('jenkins') _

node('sandbox') {

  repo = 'sample-apps'
  app = 'springboot-kafka-app'

  try {
    ws("workspace/${repo}") {

      stage_git()

      def gitDiff = sh script: "git diff --name-only master", returnStdout: true
      def files = gitDiff.split("\n")
      // for (file in gitDiff) {
      //     file = file.trim()
      //     echo "${file}"
      // }
      def dirs = sh script: "ls -d */ | cut -f1 -d'/' ", returnStdout: true
      // def dirs = sh script: "ls -d */", returnStdout: true
      def sampleApps = dirs.split("\n")
      for (sampleApp in sampleApps) {
          sampleApp = sampleApp.trim()
          echo "${sampleApp}"
        for (file in files) {
          file = file.trim()
          def appChange = file.startsWith(sampleApp)
          echo "File: ${file}, begins with: ${sampleApp}, - ${appChange}"
        }
      }

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