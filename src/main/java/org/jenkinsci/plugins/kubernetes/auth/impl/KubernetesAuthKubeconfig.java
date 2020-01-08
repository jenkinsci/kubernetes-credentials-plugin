package org.jenkinsci.plugins.kubernetes.auth.impl;

import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuth;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthException;

import java.io.IOException;

/**
 * Kubernetes authentication using a raw kubeconfig string.
 */
public class KubernetesAuthKubeconfig implements KubernetesAuth {
    private final String kubeconfig;

    public KubernetesAuthKubeconfig(String kubeconfig) {
        this.kubeconfig = kubeconfig;
    }

    @Override
    public String buildKubeConfig(KubernetesAuthConfig config) {
        return getKubeconfig();
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException {
        try {
            return new ConfigBuilder(io.fabric8.kubernetes.client.Config.fromKubeconfig(getKubeconfig()));
        } catch (IOException e) {
            throw new KubernetesAuthException(e);
        }
    }

    public String getKubeconfig() {
        return kubeconfig;
    }

}
