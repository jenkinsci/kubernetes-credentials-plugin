package org.jenkinsci.plugins.kubernetes.auth;

import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class KubernetesAuthUsernamePasswordTest {
    @Test
    public void createConfig() throws IOException {
        KubernetesAuthUsernamePassword b = new KubernetesAuthUsernamePassword("user", "pass");
        Config c = Serialization.yamlMapper().readValue(
                b.buildKubeConfig("serverUrl", "caCertificate"), Config.class
        );
        assertEquals("cluster-admin", c.getUsers().get(0).getName());
        assertEquals("user", c.getUsers().get(0).getUser().getUsername());
        assertEquals("pass", c.getUsers().get(0).getUser().getPassword());
    }
}
