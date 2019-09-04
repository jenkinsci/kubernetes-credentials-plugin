package org.jenkinsci.plugins.kubernetes.auth;

import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class KubernetesAuthTokenTest {
    @Test
    public void createConfig() throws IOException {
        KubernetesAuthToken b = new KubernetesAuthToken("t0k3n");
        Config c = Serialization.yamlMapper().readValue(
                b.buildKubeConfig("serverUrl", "caCertificate"), Config.class
        );
        assertEquals("cluster-admin", c.getUsers().get(0).getName());
        assertEquals("t0k3n", c.getUsers().get(0).getUser().getToken());
    }
}
