package org.jenkinsci.plugins.kubernetes.tokensource;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthToken;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

@Extension
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
