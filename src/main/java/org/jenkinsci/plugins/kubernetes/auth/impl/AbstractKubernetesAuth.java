package org.jenkinsci.plugins.kubernetes.auth.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.api.model.Cluster;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuth;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthException;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;

abstract class AbstractKubernetesAuth implements KubernetesAuth {
    abstract AuthInfoBuilder decorate(AuthInfoBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException;

    public String buildKubeConfig(KubernetesAuthConfig config) throws JsonProcessingException, KubernetesAuthException {
        io.fabric8.kubernetes.api.model.ConfigBuilder configBuilder = buildConfigBuilder(config, "k8s", "k8s", "cluster-admin");
        return SerializationUtils.getMapper().writeValueAsString(configBuilder.build());
    }

    public io.fabric8.kubernetes.api.model.ConfigBuilder buildConfigBuilder(KubernetesAuthConfig config, String context, String clusterName, String username) throws KubernetesAuthException {
        io.fabric8.kubernetes.api.model.ConfigBuilder configBuilder = new io.fabric8.kubernetes.api.model.ConfigBuilder();
        // setup cluster
        Cluster cluster = new Cluster();
        cluster.setServer(config.getServerUrl());
        String caCertificate = config.getCaCertificate();
        if (caCertificate != null && !caCertificate.isEmpty()) {
            cluster.setCertificateAuthorityData(Utils.encodeBase64(Utils.wrapCertificate(caCertificate)));
        }
        cluster.setInsecureSkipTlsVerify(config.isSkipTlsVerify());
        configBuilder
                .addNewCluster()
                    .withName(clusterName)
                    .withCluster(cluster)
                .endCluster();

        // setup user (class-specific)
        configBuilder
                .addNewUser()
                .withName(username)
                    .withUser(decorate(new AuthInfoBuilder(), config).build())
                .endUser();
        // setup context
        configBuilder
                .addNewContext()
                    .withName(context)
                    .withNewContext()
                        .withCluster(clusterName)
                        .withUser(username)
                    .endContext()
                .endContext();
        return configBuilder;
    }

}
