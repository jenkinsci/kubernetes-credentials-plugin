package org.jenkinsci.plugins.kubernetes.tokensource;

import com.google.jenkins.plugins.credentials.oauth.GoogleOAuth2ScopeRequirement;
import com.google.jenkins.plugins.credentials.oauth.GoogleRobotCredentials;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthToken;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * Converts {@link GoogleRobotCredentials} to {@link String} token.
 */
@Extension(optional = true)
public class GoogleRobotCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthToken, GoogleRobotCredentials>  {
    public GoogleRobotCredentialsTokenSource() {
        super(KubernetesAuthToken.class, GoogleRobotCredentials.class);
    }

    @Nonnull
    @Override
    public KubernetesAuthToken convert(@Nonnull GoogleRobotCredentials credential) {
        return new KubernetesAuthToken((serviceAddress, caCertData, skipTlsVerify) -> credential.getAccessToken(new GoogleOAuth2ScopeRequirement() {
            @Override
            public Collection<String> getScopes() {
                return Collections.singleton("https://www.googleapis.com/auth/cloud-platform");
            }
        }).getPlainText());
    }
}
