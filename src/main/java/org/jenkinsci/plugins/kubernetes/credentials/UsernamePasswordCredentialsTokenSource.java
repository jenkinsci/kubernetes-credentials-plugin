package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthUsernamePassword;

@Extension
public class UsernamePasswordCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthUsernamePassword, StandardUsernamePasswordCredentials> {
    public UsernamePasswordCredentialsTokenSource() {
        super(KubernetesAuthUsernamePassword.class, StandardUsernamePasswordCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthUsernamePassword convert(@NonNull StandardUsernamePasswordCredentials credential) throws AuthenticationTokenException {
        return new KubernetesAuthUsernamePassword(
                credential.getUsername(),
                credential.getPassword().getPlainText()
        );
    }
}
