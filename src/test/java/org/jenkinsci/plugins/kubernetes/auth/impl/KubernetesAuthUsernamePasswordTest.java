package org.jenkinsci.plugins.kubernetes.auth.impl;

import static org.junit.Assert.assertEquals;

import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.Secret;

public class KubernetesAuthUsernamePasswordTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void createConfig() throws Exception {
        KubernetesAuthUsernamePassword b = new KubernetesAuthUsernamePassword("user", Secret.fromString("pass"));
        io.fabric8.kubernetes.api.model.Config c = b.buildConfigBuilder(new KubernetesAuthConfig("serverUrl", "caCertificate", false), "k8s", "k8s", "cluster-admin").build();
        assertEquals("cluster-admin", c.getUsers().get(0).getName());
        assertEquals("user", c.getUsers().get(0).getUser().getUsername());
        assertEquals("pass", c.getUsers().get(0).getUser().getPassword());
    }
}
