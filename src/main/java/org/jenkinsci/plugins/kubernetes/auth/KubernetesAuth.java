package org.jenkinsci.plugins.kubernetes.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.client.ConfigBuilder;

/**
 * Abstracts away a Kubernetes authentication either through kubeconfig format or {@link ConfigBuilder}.
 */
public interface KubernetesAuth {
    /**
     * Decorates a {@link ConfigBuilder} to connect using the current authentication object.
     * @param builder the configuration to decorate
     * @return the decorated configuration
     * @throws KubernetesAuthException if anything fails during the processing of the authentication configuration
     */
    ConfigBuilder decorate(ConfigBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException;

    /**
     * Builds a kube config file content based on the current authentication object.
     *
     * @return Kubeconfig file content corresponding to this authentication object.
     * @throws JsonProcessingException if something fails while generating the json document for kubeconfig
     * @throws KubernetesAuthException if something fails while dealing with credentials
     */
    String buildKubeConfig(KubernetesAuthConfig config) throws JsonProcessingException, KubernetesAuthException;
    io.fabric8.kubernetes.api.model.ConfigBuilder buildConfigBuilder(KubernetesAuthConfig config,String context,String clusterName,String username) throws KubernetesAuthException;
}
