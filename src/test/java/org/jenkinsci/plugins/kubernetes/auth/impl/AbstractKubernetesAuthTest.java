package org.jenkinsci.plugins.kubernetes.auth.impl;

import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthException;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractKubernetesAuthTest {
    @Test
    public void createConfig() throws Exception {
        AbstractKubernetesAuth b = new AbstractKubernetesAuth() {
            @Override
            public ConfigBuilder decorate(ConfigBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException {
                return builder;
            }

            @Override
            AuthInfoBuilder decorate(AuthInfoBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException {
                return builder;
            }
        };
        io.fabric8.kubernetes.api.model.Config c = Serialization.yamlMapper().readValue(
                b.buildKubeConfig(new KubernetesAuthConfig("serverUrl", "caCertificate", false)), io.fabric8.kubernetes.api.model.Config.class
        );
        assertEquals("serverUrl", c.getClusters().get(0).getCluster().getServer());
        assertEquals(Utils.encodeBase64(Utils.wrapCertificate("caCertificate")), c.getClusters().get(0).getCluster().getCertificateAuthorityData());
    }
}
