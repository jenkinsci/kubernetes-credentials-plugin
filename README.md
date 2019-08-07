# Kubernetes Credentials Plugin

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/kubernetes-credentials.svg)](https://plugins.jenkins.io/kubernetes-credentials)
[![travis-ci](https://travis-ci.org/jenkinsci/kubernetes-credentials-plugin.svg?branch=master)](https://travis-ci.org/jenkinsci/kubernetes-credentials-plugin)
[![Coverage Status](https://coveralls.io/repos/github/jenkinsci/kubernetes-credentials-plugin/badge.svg?branch=master)](https://coveralls.io/github/jenkinsci/kubernetes-credentials-plugin?branch=master)
[![Jenkins Plugin installs](https://img.shields.io/jenkins/plugin/i/kubernetes-credentials.svg)](https://plugins.jenkins.io/kubernetes-credentials)

Contains classes shared between the [Kubernetes Plugin][kubernetes-plugin] and the
[Kubernetes CLI Plugin][kubernetes-cli-plugin]:
* an OpenShift username/password credentials that can fetch a valid Kubernetes token when needed
* an OpenShift secret credentials, which is meant to hold a Kubernetes token

Those credentials are available through the UI and in pipelines.

## Building and Testing
To build the extension, run:
```bash
mvn clean package
```
and upload `target/kubernetes-credentials.hpi` to your Jenkins installation.

To run the tests:
```bash
mvn clean test
```

## Releasing
```bash
mvn release:prepare release:perform
```

[kubernetes-plugin]:https://github.com/jenkinsci/kubernetes-plugin
[kubernetes-cli-plugin]:https://github.com/jenkinsci/kubernetes-cli-plugin
[master-build]: https://ci.jenkins.io/job/Plugins/job/kubernetes-credentials-plugin/job/master/
