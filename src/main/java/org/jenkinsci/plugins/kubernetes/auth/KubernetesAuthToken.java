package org.jenkinsci.plugins.kubernetes.auth;

import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;

public class KubernetesAuthToken extends AbstractKubernetesAuth implements KubernetesAuth {
    private final String token;

    public KubernetesAuthToken(String token) {
        this.token = token;
    }

    @Override
    public void decorate(AuthInfoBuilder authInfoBuilder) {
        authInfoBuilder.withToken(getToken());
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
