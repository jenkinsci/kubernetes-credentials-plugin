package org.jenkinsci.plugins.kubernetes.auth;

import hudson.FilePath;
import io.fabric8.kubernetes.client.ConfigBuilder;

public interface KubernetesAuth {
    String generateKubectlConfigArguments(FilePath workspace);

    ConfigBuilder decorate(ConfigBuilder builder) throws KubernetesAuthException;
}
