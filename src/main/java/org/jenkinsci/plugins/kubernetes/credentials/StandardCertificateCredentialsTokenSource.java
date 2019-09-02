package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.apache.commons.codec.binary.Base64;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthCertificate;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

@Extension
public class StandardCertificateCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthCertificate, StandardCertificateCredentials> {
    public StandardCertificateCredentialsTokenSource() {
        super(KubernetesAuthCertificate.class, StandardCertificateCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthCertificate convert(@NonNull StandardCertificateCredentials credential) throws AuthenticationTokenException {
        try {
            KeyStore keyStore = credential.getKeyStore();
            String password = credential.getPassword().getPlainText();
            String alias = keyStore.aliases().nextElement();
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
            return new KubernetesAuthCertificate(
                    Utils.wrapCertificate(Base64.encodeBase64String(certificate.getEncoded())),
                    Utils.wrapPrivateKey(Base64.encodeBase64String(keyStore.getKey(alias, password.toCharArray()).getEncoded())),
                    password
            );

        } catch (KeyStoreException | CertificateEncodingException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw new AuthenticationTokenException(e.getMessage());
        }
    }
}
