package org.jenkinsci.plugins.kubernetes.auth;

import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;

public class KubernetesAuthCertificate extends AbstractKubernetesAuth implements KubernetesAuth {
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
