package org.jenkinsci.plugins.kubernetes.auth;

import hudson.FilePath;
import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;

public class KubernetesAuthCertificate implements KubernetesAuth {
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
    public void decorate(AuthInfoBuilder builder) {
        builder.withClientCertificateData(certificate).withClientKeyData(key);
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder) throws KubernetesAuthException {
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
