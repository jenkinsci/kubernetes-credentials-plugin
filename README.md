# Kubernetes Credentials Plugin

[![build-status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/kubernetes-credentials-plugin/master/)][master-build]
![Jenkins Plugin installs](https://img.shields.io/jenkins/plugin/i/kubernetes-credentials.svg)

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
