package org.jenkinsci.plugins.kubernetes.auth.impl;

import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuth;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthException;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;

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
    public ConfigBuilder decorate(ConfigBuilder builder, KubernetesAuthConfig config) {
        return new ConfigBuilder(io.fabric8.kubernetes.client.Config.fromKubeconfig(getKubeconfig()));
    }

    public io.fabric8.kubernetes.api.model.ConfigBuilder buildConfigBuilder(KubernetesAuthConfig config, String context, String clusterName, String username) throws KubernetesAuthException {
        try {
            io.fabric8.kubernetes.api.model.Config kubeConfig = KubeConfigUtils.parseConfigFromString(getKubeconfig());
            return new io.fabric8.kubernetes.api.model.ConfigBuilder(kubeConfig);
        } catch (Exception e) {
            throw new KubernetesAuthException(e.getMessage(), e);
        }
    }

    public String getKubeconfig() {
        return kubeconfig;
    }
}
