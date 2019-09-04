package org.jenkinsci.plugins.kubernetes.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.api.model.Cluster;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;

public class KubernetesAuthCertificate extends KubeConfigBuilder implements KubernetesAuth {
    private final String certificate;

    private final String key;

    private final String password;


   public KubernetesAuthCertificate(String certificate, String key, String password) {
        this.certificate = certificate;
        this.key = key;
        this.password = password;
    }

    public KubernetesAuthCertificate(String certificate, String key) {
        this(certificate, key, null);
    }

    @Override
    public void decorate(AuthInfoBuilder authInfoBuilder) {
        authInfoBuilder.withClientCertificateData(getCertificate()).withClientKeyData(getKey());
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder) {
        builder.withClientCertData(certificate);
        builder.withClientKeyData(key);
        if (password != null) {
            builder.withClientKeyPassphrase(password);
        }
        return builder;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getKey() {
        return key;
    }

    public String getKeyPassword() {
        return password;
    }
}
