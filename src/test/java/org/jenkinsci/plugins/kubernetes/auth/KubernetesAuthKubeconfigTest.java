package org.jenkinsci.plugins.kubernetes.auth;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class KubernetesAuthKubeconfigTest {
    @Test
    public void createConfig() throws IOException {
        KubernetesAuthKubeconfig b = new KubernetesAuthKubeconfig("data");

        assertEquals("data", b.buildKubeConfig("serverUrl", "caCertificate"));
    }

}
