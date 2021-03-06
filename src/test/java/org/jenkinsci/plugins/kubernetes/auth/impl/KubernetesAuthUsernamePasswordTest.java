package org.jenkinsci.plugins.kubernetes.auth.impl;

import hudson.util.Secret;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthUsernamePassword;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

public class KubernetesAuthUsernamePasswordTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void createConfig() throws Exception {
        KubernetesAuthUsernamePassword b = new KubernetesAuthUsernamePassword("user", Secret.fromString("pass"));
        io.fabric8.kubernetes.api.model.Config c = Serialization.yamlMapper().readValue(
                b.buildKubeConfig(new KubernetesAuthConfig("serverUrl", "caCertificate", false)), io.fabric8.kubernetes.api.model.Config.class
        );
        assertEquals("cluster-admin", c.getUsers().get(0).getName());
        assertEquals("user", c.getUsers().get(0).getUser().getUsername());
        assertEquals("pass", c.getUsers().get(0).getUser().getPassword());
    }
}
