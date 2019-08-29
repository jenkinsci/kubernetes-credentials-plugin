package org.jenkinsci.plugins.kubernetes.auth;

public class KubernetesAuthKubeconfig implements KubernetesAuth {
    private final String kubeconfig;

    public KubernetesAuthKubeconfig(String kubeconfig) {
        this.kubeconfig = kubeconfig;
    }

    public String getKubeconfig() {
        return kubeconfig;
    }

}
