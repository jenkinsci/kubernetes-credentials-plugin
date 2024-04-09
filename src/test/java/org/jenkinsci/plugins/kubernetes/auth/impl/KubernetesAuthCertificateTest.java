package org.jenkinsci.plugins.kubernetes.auth.impl;

import static org.junit.Assert.assertEquals;

import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.Secret;

public class KubernetesAuthCertificateTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void createConfig() throws Exception {
        String certData = Utils.wrapCertificate("cert_data");
        String keyData = Utils.wrapPrivateKey("key_data");
        KubernetesAuthCertificate b = new KubernetesAuthCertificate(
                certData,
                Secret.fromString(keyData)
        );
        io.fabric8.kubernetes.api.model.Config c = b.buildConfigBuilder(new KubernetesAuthConfig("serverUrl", "caCertificate", false), "k8s", "k8s", "cluster-admin").build();
        // verifying class doesn't modify cert and key data, so not using here
        assertEquals(Utils.encodeBase64(certData), c.getUsers().get(0).getUser().getClientCertificateData());
        assertEquals(Utils.encodeBase64(keyData), c.getUsers().get(0).getUser().getClientKeyData());
    }
}
