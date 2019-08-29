package org.jenkinsci.plugins.kubernetes.auth;

public class KubernetesAuthToken implements KubernetesAuth {
    private final String token;

    public KubernetesAuthToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
