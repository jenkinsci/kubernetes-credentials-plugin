package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthUsernamePassword;

public class UsernamePasswordCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthUsernamePassword, UsernamePasswordCredentials> {
    public UsernamePasswordCredentialsTokenSource() {
        super(KubernetesAuthUsernamePassword.class, UsernamePasswordCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthUsernamePassword convert(@NonNull UsernamePasswordCredentials credential) throws AuthenticationTokenException {
        return new KubernetesAuthUsernamePassword(
                credential.getUsername(),
                credential.getPassword().getPlainText()
        );
    }
}
