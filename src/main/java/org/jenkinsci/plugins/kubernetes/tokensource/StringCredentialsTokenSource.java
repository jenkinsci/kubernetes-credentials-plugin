package org.jenkinsci.plugins.kubernetes.tokensource;

import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthToken;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import edu.umd.cs.findbugs.annotations.NonNull;

@Extension(optional = true)
public class StringCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthToken, StringCredentials> {
    public StringCredentialsTokenSource() {
        super(KubernetesAuthToken.class, StringCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthToken convert(@NonNull StringCredentials credential) {
        return new KubernetesAuthToken((serviceAddress, caCertData, skipTlsVerify) -> credential.getSecret().getPlainText());
    }
}
