package org.jenkinsci.plugins.kubernetes.credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthToken;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class StringCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthToken, StringCredentials> {
    public StringCredentialsTokenSource() {
        super(KubernetesAuthToken.class, StringCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthToken convert(@NonNull StringCredentials credential) throws AuthenticationTokenException {
        return new KubernetesAuthToken(credential.getSecret().getPlainText());
    }
}
