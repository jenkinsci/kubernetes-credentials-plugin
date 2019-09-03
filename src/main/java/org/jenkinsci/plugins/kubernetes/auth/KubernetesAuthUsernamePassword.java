package org.jenkinsci.plugins.kubernetes.auth;

import hudson.FilePath;
import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;

public class KubernetesAuthUsernamePassword implements KubernetesAuth {
    private final String username;

    private final String password;


    public KubernetesAuthUsernamePassword(String username, String password) {

        this.username = username;
        this.password = password;
    }

    @Override
    public void decorate(AuthInfoBuilder builder) {
        builder.withUsername(username).withPassword(password);
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
