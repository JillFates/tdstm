#!/usr/bin/env groovy

node {
    def options = [
        git_repository                   : 'ssh://git@git.tdsops.com:7999/tm/tdstm.git',
        git_credentials                  : 'ad45d40d-c460-4878-a4d0-87b02297fcfc',
        git_branch                       : 'feat/TM-15566_chrome-driver',
        kubernetes_secrets               : ['tm-registry'],
    ]
    def tmlabel = "Playground-${BUILD_NUMBER}-${UUID.randomUUID().toString()}"
    // def containers = [containerTemplate(name: 'docker-compose',
    //                                     image: 'tm-registry.transitionmanager.net/tds-ci/docker-compose:1.1',
    //                                     ttyEnabled: true,
    //                                     privileged: true,
    //                                     command: 'cat')]
    def yaml = """
apiVersion: v1
kind: Pod
metadata:
  labels:
    docker-compose: true
spec:
  containers:
    - name: docker-compose
      image: tm-registry.transitionmanager.net/tds-ci/tm-docker-compose:latest
      command:
      - cat
      tty: true
      volumeMounts:
      - mountPath: /var/run/docker.sock
        name: docker-socket-volume
      securityContext:
        allowPrivilegeEscalation: true
        privileged: true
  volumes:
    - name: docker-socket-volume
      hostPath:
        path: /var/run/docker.sock
        type: File
"""

    podTemplate(label: tmlabel,
                imagePullSecrets: options.kubernetes_secrets,
                yaml: yaml) {
      node(tmlabel){
        stage('Clone') {
          container('docker-compose') {
            // checkout scm
            checkout([
                        $class: 'GitSCM',
                        branches: [[name: options.git_branch]],
                        userRemoteConfigs: [[
                            url: options.git_repository,
                            credentialsId: options.git_credentials,
                        ]],
                        extensions: [[
                                $class: 'CloneOption',
                                shallow: true,
                                depth: 100,
                                noTags: false,
                                reference: '',
                                timeout: 15
                            ]]
                    ])
          }
        }

        stage('Build For Testing') {
          container('docker-compose') {
            sh "cat Dockerfile"
            sh "docker-compose -p ${env.BUILD_ID} build e2e"
          }
        }

        stage('Test') {
          container('docker-compose') {
            try {
                sh "docker-compose -p ${env.BUILD_ID} run e2e cd /opt/tdstm/test/e2e && ls -hal"
                sh "docker-compose -p ${env.BUILD_ID} run e2e ls -hal"
                sh "docker-compose -p ${env.BUILD_ID} run e2e pwd"
                sh "docker-compose -p ${env.BUILD_ID} run e2e cd /opt/tdstm && ls -hal"
                sh "docker-compose -p ${env.BUILD_ID} run e2e"
            } finally {
                sh "docker-compose -p ${env.BUILD_ID} down --remove-orphans"
                sh "docker-compose -p ${env.BUILD_ID} rm"
            }
          }
        }
      }

    }
}
