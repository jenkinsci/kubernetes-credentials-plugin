package org.jenkinsci.plugins.kubernetes.auth;

import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;

import java.io.IOException;

public class KubernetesAuthKubeconfig implements KubernetesAuth {
    private final String kubeconfig;

    public KubernetesAuthKubeconfig(String kubeconfig) {
        this.kubeconfig = kubeconfig;
    }

    @Override
    public void decorate(AuthInfoBuilder builder) {
        //
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder) throws KubernetesAuthException {
        try {
            return new ConfigBuilder(Config.fromKubeconfig(getKubeconfig()));
        } catch (IOException e) {
            throw new KubernetesAuthException(e.getMessage());
        }
    }

    public String getKubeconfig() {
        return kubeconfig;
    }

}
