package org.jenkinsci.plugins.kubernetes.auth;

import hudson.FilePath;
import io.fabric8.kubernetes.api.model.AuthInfo;
import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;

public interface KubernetesAuth {
    void decorate(AuthInfoBuilder builder);

    ConfigBuilder decorate(ConfigBuilder builder) throws KubernetesAuthException;
}
