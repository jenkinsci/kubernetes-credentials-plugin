package org.jenkinsci.plugins.kubernetes.credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import com.google.jenkins.plugins.credentials.oauth.GoogleRobotCredentials;
import com.google.jenkins.plugins.credentials.oauth.GoogleOAuth2ScopeRequirement;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthToken;

import java.util.Collection;
import java.util.Collections;

/**
 * Converts {@link GoogleRobotCredentials} to {@link String} token.
 */
@Extension
public class GoogleRobotCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthToken, GoogleRobotCredentials>  {
    public GoogleRobotCredentialsTokenSource() {
        super(KubernetesAuthToken.class, GoogleRobotCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthToken convert(@NonNull GoogleRobotCredentials credential) throws AuthenticationTokenException {
        return new KubernetesAuthToken(credential.getAccessToken(new GoogleOAuth2ScopeRequirement() {
            @Override
            public Collection<String> getScopes() {
                return Collections.singleton("https://www.googleapis.com/auth/cloud-platform");
            }
        }).getPlainText());
    }
}
