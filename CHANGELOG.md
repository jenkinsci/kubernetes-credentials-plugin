CHANGELOG
=========

Master (unreleased)
-----

0.10.0
-----
* Update kubernetes-client-api to 5.4.1 [#35](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/35)
* Upgrade kubernetes-client to 6.x [#36](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/36)
* Have *KubernetesAuth able to directly access Secret instead of String [#33](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/33)
* [\doc\] Noting broader usage scope [#34](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/34)

0.9.0
-----
* \[test\] Run tests against Java 11 [#30](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/30)
* use Secret instead of String for sensitive data [#31](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/31)
* change minimal Jenkins version for 2.222.1 LTS [42edb2ef](https://github.com/jenkinsci/kubernetes-credentials-plugin/commit/42edb2efffb1415f055975667f351f13ed8f4642)
* more useful error messages with OpenShift [#32](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/32)

0.8.0
-----
* Add JCasC test to this plugin [#24](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/24)
* Openshift oauth discover authorization_endpoint [#26](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/26)


0.7.0
-----
* Some plugins could be optional [#20](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/20)
* Compatibility with Jenkins 2.235 [#22](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/22)
* Delete obsolete OpenShiftTokenCredentialImpl/credentials.jelly [#23](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/23)


0.6.2
-----
* [JENKINS-60798] Current context should be set [#19](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/19)

0.6.1
-----
*Release failed*

0.6.0
-----
* Give access to ConfigBuilder [#17](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/17)
* Remove additional carrier return when reading fixture [#16](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/16)

0.5.0
-----
* Update URL for plugins.jenkins.io [#14](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/14)
* Enable incrementals support [#13](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/13)
* enable support of AuthenticationTokens, add Google OAuth Credentials Token Source [#12](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/12)
* README: Add Jenkins plugin site badges[#11](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/11)

0.4.1
-----
* Plugin description should live in index.jelly [#10](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/10)

0.4.0
-----
* Fix compatibility with PCT [#4](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/4)
* Require Jenkins core 2.60.3
* Remove packaged apache-httpclient and depend on [apache-httpcomponents-client-4-api-plugin](https://github.com/jenkinsci/apache-httpcomponents-client-4-api-plugin) instead.

0.3.1
-----
* Save OpenShift token between calls [#3](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/3)
* Cache OpenShift token per URL
* Improve plugin testing

0.3.0
-----
* Improve README
* More meaningful credential names [#2](https://github.com/jenkinsci/kubernetes-credentials-plugin/pull/2)


0.2.0
-----
* Initial release; extracted from [kubernetes-plugin](https://github.com/jenkinsci/kubernetes-plugin) starting with version 1.2.
