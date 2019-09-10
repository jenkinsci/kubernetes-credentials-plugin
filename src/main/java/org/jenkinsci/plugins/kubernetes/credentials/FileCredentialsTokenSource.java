package org.jenkinsci.plugins.kubernetes.credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthKubeconfig;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Extension
public class FileCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthKubeconfig, FileCredentials> {
    public FileCredentialsTokenSource() {
        super(KubernetesAuthKubeconfig.class, FileCredentials.class);
    }

    @NonNull
    @Override
    public KubernetesAuthKubeconfig convert(@NonNull FileCredentials credential) throws AuthenticationTokenException {
        try (InputStream is = credential.getContent()) {
            return new KubernetesAuthKubeconfig(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AuthenticationTokenException(e);
        }
    }
}
