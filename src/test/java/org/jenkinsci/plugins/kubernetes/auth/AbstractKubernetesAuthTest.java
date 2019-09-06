package org.jenkinsci.plugins.kubernetes.auth;

import static org.junit.Assert.*;

import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;
import org.junit.Test;

import java.io.IOException;

public class AbstractKubernetesAuthTest {
    @Test
    public void createConfig() throws IOException {
        AbstractKubernetesAuth b = new AbstractKubernetesAuth() {
            @Override
            void decorate(AuthInfoBuilder authInfoBuilder) {

            }

            @Override
            public ConfigBuilder decorate(ConfigBuilder builder) {
                return builder;
            }
        };
        Config c = Serialization.yamlMapper().readValue(
                b.buildKubeConfig("serverUrl", "caCertificate"), Config.class
        );
        assertEquals("serverUrl", c.getClusters().get(0).getCluster().getServer());
        assertEquals(Utils.wrapCertificate("caCertificate"), c.getClusters().get(0).getCluster().getCertificateAuthorityData());
    }
}
