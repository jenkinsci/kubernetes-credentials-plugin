package org.jenkinsci.plugins.kubernetes.auth.impl;

import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuth;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthException;

/**
 * Kubernetes Authentication using a username and password
 */
public class KubernetesAuthUsernamePassword extends AbstractKubernetesAuth implements KubernetesAuth {
    private final String username;
    private final String password;


    public KubernetesAuthUsernamePassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public AuthInfoBuilder decorate(AuthInfoBuilder authInfoBuilder, KubernetesAuthConfig config) {
        return authInfoBuilder
                .withUsername(getUsername())
                .withPassword(getPassword());
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException {
        return builder
                .withUsername(getUsername())
                .withPassword(getPassword());
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
