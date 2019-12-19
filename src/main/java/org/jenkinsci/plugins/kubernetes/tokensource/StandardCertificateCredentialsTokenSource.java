package org.jenkinsci.plugins.kubernetes.tokensource;

import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthKeystore;

@Extension
public class StandardCertificateCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthKeystore, StandardCertificateCredentials> {
    public StandardCertificateCredentialsTokenSource() {
        super(KubernetesAuthKeystore.class, StandardCertificateCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthKeystore convert(@NonNull StandardCertificateCredentials credential) throws AuthenticationTokenException {
        return new KubernetesAuthKeystore(credential.getKeyStore(), Secret.toString(credential.getPassword()));
    }
}
