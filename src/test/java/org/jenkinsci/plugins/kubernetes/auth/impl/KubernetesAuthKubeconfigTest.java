package org.jenkinsci.plugins.kubernetes.auth.impl;

import static org.junit.Assert.assertEquals;

import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.junit.Test;

public class KubernetesAuthKubeconfigTest {
    @Test
    public void createConfig() throws Exception {
        KubernetesAuthKubeconfig b = new KubernetesAuthKubeconfig("data");

        assertEquals("data", b.buildKubeConfig(new KubernetesAuthConfig("serverUrl", "caCertificate", false)));
    }

}
