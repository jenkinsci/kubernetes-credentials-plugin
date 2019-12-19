package org.jenkinsci.plugins.kubernetes.auth.impl;

import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthKubeconfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KubernetesAuthKubeconfigTest {
    @Test
    public void createConfig() throws Exception {
        KubernetesAuthKubeconfig b = new KubernetesAuthKubeconfig("data");

        assertEquals("data", b.buildKubeConfig(new KubernetesAuthConfig("serverUrl", "caCertificate", false)));
    }

}
