package org.jenkinsci.plugins.kubernetes.auth.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthException;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;
import org.junit.Test;

import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.api.model.Cluster;
import io.fabric8.kubernetes.client.ConfigBuilder;

public class AbstractKubernetesAuthTest {
    @Test
    public void createConfig() throws Exception {
        AbstractKubernetesAuth b = new NoopKubernetesAuth();
        io.fabric8.kubernetes.api.model.Config c = b.buildConfigBuilder(new KubernetesAuthConfig("serverUrl", "caCertificate", false), "k8s", "k8s", "cluster-admin").build();
        assertEquals(1, c.getClusters().size());
        Cluster cluster = c.getClusters().get(0).getCluster();
        assertEquals("serverUrl", cluster.getServer());
        assertNull(cluster.getInsecureSkipTlsVerify());
        assertEquals("k8s",c.getCurrentContext());
        assertEquals(Utils.encodeBase64(Utils.wrapCertificate("caCertificate")), cluster.getCertificateAuthorityData());
    }

    @Test
    public void skipTlsVerify() throws Exception {
        AbstractKubernetesAuth b = new NoopKubernetesAuth();
        io.fabric8.kubernetes.api.model.Config c = b.buildConfigBuilder(new KubernetesAuthConfig("serverUrl", "caCertificate", true), "k8s", "k8s", "cluster-admin").build();
        assertEquals(1, c.getClusters().size());
        Cluster cluster = c.getClusters().get(0).getCluster();
        assertEquals("serverUrl", cluster.getServer());
        assertTrue(cluster.getInsecureSkipTlsVerify());
        assertEquals("k8s",c.getCurrentContext());
        assertEquals(Utils.encodeBase64(Utils.wrapCertificate("caCertificate")), cluster.getCertificateAuthorityData());
    }

    private static class NoopKubernetesAuth extends AbstractKubernetesAuth {
        @Override
        public ConfigBuilder decorate(ConfigBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException {
            return builder;
        }

        @Override
        AuthInfoBuilder decorate(AuthInfoBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException {
            return builder;
        }
    }
}
