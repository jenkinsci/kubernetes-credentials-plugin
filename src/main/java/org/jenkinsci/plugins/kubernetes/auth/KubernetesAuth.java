package org.jenkinsci.plugins.kubernetes.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.client.ConfigBuilder;

public interface KubernetesAuth {
    ConfigBuilder decorate(ConfigBuilder builder) throws KubernetesAuthException;

    String buildKubeConfig(String serverUrl, String caCertificate) throws JsonProcessingException;
}
