package org.jenkinsci.plugins.kubernetes.tokensource;

import com.google.jenkins.plugins.credentials.oauth.GoogleOAuth2ScopeRequirement;
import com.google.jenkins.plugins.credentials.oauth.GoogleRobotCredentials;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthToken;

import java.util.Collection;
import java.util.Collections;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Converts {@link GoogleRobotCredentials} to {@link String} token.
 */
@Extension(optional = true)
public class GoogleRobotCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthToken, GoogleRobotCredentials>  {
    public GoogleRobotCredentialsTokenSource() {
        super(KubernetesAuthToken.class, GoogleRobotCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthToken convert(@NonNull GoogleRobotCredentials credential) {
        return new KubernetesAuthToken((serviceAddress, caCertData, skipTlsVerify) -> credential.getAccessToken(new InnerGoogleOAuth2ScopeRequirement()).getPlainText());
    }

    private static class InnerGoogleOAuth2ScopeRequirement extends GoogleOAuth2ScopeRequirement {

        private static final long serialVersionUID = -4271930203103202864L;

        @Override
        public Collection<String> getScopes() {
            return Collections.singleton("https://www.googleapis.com/auth/cloud-platform");
        }
    }
}
