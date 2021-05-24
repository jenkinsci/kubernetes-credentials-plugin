package org.jenkinsci.plugins.kubernetes.auth.impl;

import hudson.util.Secret;
import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuth;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthException;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;

import javax.annotation.Nonnull;
import java.security.*;
import java.security.cert.CertificateEncodingException;

/**
 * Kubernetes authentication using certificate and private key obtained from a keystore with a passphrase.
 */
public class KubernetesAuthKeystore extends AbstractKubernetesAuth implements KubernetesAuth {
    private final Secret passPhrase;
    private KeyStore keyStore;

    public KubernetesAuthKeystore(@Nonnull KeyStore keyStore, Secret passPhrase) {
        this.keyStore = keyStore;
        this.passPhrase = passPhrase;
    }

    public KubernetesAuthKeystore(@Nonnull KeyStore keyStore, String passPhrase) {
        this(keyStore, Secret.fromString(passPhrase));
    }

    @Override
    public AuthInfoBuilder decorate(AuthInfoBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException {
        try {
            String alias = keyStore.aliases().nextElement();
            // Get private key using passphrase
            Key key = keyStore.getKey(alias, passPhrase.getPlainText().toCharArray());
            return builder
                    .withClientCertificateData(Utils.encodeCertificate(keyStore.getCertificate(alias)))
                    .withClientKeyData(Utils.encodeKey(key));
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateEncodingException e) {
            throw new KubernetesAuthException(e.getMessage(), e);
        }
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException {
        try {
            String alias = keyStore.aliases().nextElement();
            // Get private key using passphrase
            Key key = keyStore.getKey(alias, passPhrase.getPlainText().toCharArray());
            return builder
                    .withClientCertData(Utils.encodeCertificate(keyStore.getCertificate(alias)))
                    .withClientKeyData(Utils.encodeKey(key));
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateEncodingException e) {
            throw new KubernetesAuthException(e.getMessage(), e);
        }
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    @Deprecated
    public String getPassPhrase() {
        return passPhrase.getPlainText();
    }

    public Secret getPassPhraseSecret() {
        return passPhrase;
    }
}
