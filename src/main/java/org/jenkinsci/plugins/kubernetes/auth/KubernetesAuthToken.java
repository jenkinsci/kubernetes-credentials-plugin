package org.jenkinsci.plugins.kubernetes.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.api.model.Cluster;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;

public class KubernetesAuthToken implements KubernetesAuth {
    private final String token;

    public KubernetesAuthToken(String token) {
        this.token = token;
    }

    @Override
    public String buildKubeConfig(String serverUrl, String caCertificate) throws JsonProcessingException {
        io.fabric8.kubernetes.api.model.ConfigBuilder configBuilder = new io.fabric8.kubernetes.api.model.ConfigBuilder();
        // setup cluster
        Cluster cluster = new Cluster();
        cluster.setServer(serverUrl);
        if (caCertificate != null && !caCertificate.isEmpty()) {
            cluster.setCertificateAuthorityData(Utils.wrapCertificate(caCertificate));
        } else {
            cluster.setInsecureSkipTlsVerify(true);
        }
        configBuilder.addNewCluster().withName("k8s").withCluster(cluster).endCluster();

        // setup user (class-specific)
        AuthInfoBuilder authInfoBuilder = new AuthInfoBuilder();
        authInfoBuilder.withToken(getToken());

        configBuilder.addNewUser().withName("cluster-admin").withUser(authInfoBuilder.build()).endUser();
        // setup context
        configBuilder.addNewContext().withName("k8s").withNewContext().withCluster("k8s").withUser("cluster-admin").endContext().endContext();
        return SerializationUtils.getMapper().writeValueAsString(configBuilder.build());
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder) throws KubernetesAuthException {
        builder.withOauthToken(getToken());
        return builder;
    }

    public String getToken() {
        return token;
    }
}