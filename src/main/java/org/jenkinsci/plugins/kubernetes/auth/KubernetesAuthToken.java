package org.jenkinsci.plugins.kubernetes.auth;

import hudson.FilePath;
import io.fabric8.kubernetes.client.ConfigBuilder;

public class KubernetesAuthToken implements KubernetesAuth {
    private final String token;

    public KubernetesAuthToken(String token) {
        this.token = token;
    }

    @Override
    public String generateKubectlConfigArguments(FilePath workspace) {
        return "--token " + getToken();
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder) throws KubernetesAuthException {
        builder.withOauthToken(getToken());
        return builder;
    }

    public String getToken() {
        return token;
    }
}
