package org.jenkinsci.plugins.kubernetes.tokensource;

import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthKeystore;

import edu.umd.cs.findbugs.annotations.NonNull;

@Extension
public class StandardCertificateCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthKeystore, StandardCertificateCredentials> {
    public StandardCertificateCredentialsTokenSource() {
        super(KubernetesAuthKeystore.class, StandardCertificateCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthKeystore convert(@NonNull StandardCertificateCredentials credential) throws AuthenticationTokenException {
        return new KubernetesAuthKeystore(credential.getKeyStore(), credential.getPassword());
    }
}
