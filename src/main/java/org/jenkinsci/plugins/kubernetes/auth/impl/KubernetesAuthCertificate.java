package org.jenkinsci.plugins.kubernetes.auth.impl;

import hudson.util.Secret;
import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuth;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;

public class KubernetesAuthCertificate extends AbstractKubernetesAuth implements KubernetesAuth {
    private final String certificate;

    private final Secret key;

    public KubernetesAuthCertificate(String certificate, Secret key) {
        this.certificate = certificate;
        this.key = key;
    }

    @Override
    public AuthInfoBuilder decorate(AuthInfoBuilder builder, KubernetesAuthConfig config) {
        return builder
                .withClientCertificateData(Utils.encodeBase64(certificate))
                .withClientKeyData(Utils.encodeBase64(getKey()));
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder, KubernetesAuthConfig config) {
        return builder
                .withClientCertData(Utils.encodeBase64(certificate))
                .withClientKeyData(Utils.encodeBase64(getKey()));
    }

    public String getCertificate() {
        return certificate;
    }

    public String getKey() {
        return key.getPlainText();
    }
}
