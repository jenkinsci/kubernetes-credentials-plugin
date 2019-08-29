package org.jenkinsci.plugins.kubernetes.auth;

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
