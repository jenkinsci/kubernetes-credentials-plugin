package org.jenkinsci.plugins.kubernetes.auth;

import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;

public class KubernetesAuthUsernamePassword extends AbstractKubernetesAuth implements KubernetesAuth {
    private final String username;
    private final String password;


    public KubernetesAuthUsernamePassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void decorate(AuthInfoBuilder authInfoBuilder) {
        authInfoBuilder.withUsername(getUsername()).withPassword(getPassword());
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder) throws KubernetesAuthException {
        builder.withUsername(getUsername());
        builder.withPassword(getPassword());
        return builder;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
