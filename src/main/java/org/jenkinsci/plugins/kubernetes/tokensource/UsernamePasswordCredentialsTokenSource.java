package org.jenkinsci.plugins.kubernetes.tokensource;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuth;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthToken;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthUsernamePassword;
import org.jenkinsci.plugins.kubernetes.credentials.TokenProducer;

@Extension
public class UsernamePasswordCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuth, StandardUsernamePasswordCredentials> {
    public UsernamePasswordCredentialsTokenSource() {
        super(KubernetesAuth.class, StandardUsernamePasswordCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuth convert(@NonNull StandardUsernamePasswordCredentials credential) throws AuthenticationTokenException {
        if (credential instanceof TokenProducer) {
            return new KubernetesAuthToken((TokenProducer) credential);
        } else {
            return new KubernetesAuthUsernamePassword(
                    credential.getUsername(),
                    credential.getPassword().getPlainText()
            );
        }
    }
}
