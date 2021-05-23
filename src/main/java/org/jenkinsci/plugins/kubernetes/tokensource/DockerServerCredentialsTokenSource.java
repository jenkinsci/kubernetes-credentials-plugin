package org.jenkinsci.plugins.kubernetes.tokensource;

import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.docker.commons.credentials.DockerServerCredentials;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthCertificate;

import javax.annotation.Nonnull;

@Extension(optional = true)
public class DockerServerCredentialsTokenSource extends AuthenticationTokenSource<KubernetesAuthCertificate, DockerServerCredentials> {
    public DockerServerCredentialsTokenSource() { super(KubernetesAuthCertificate.class, DockerServerCredentials.class); }

    @Nonnull
    @Override
    public KubernetesAuthCertificate convert(@Nonnull DockerServerCredentials credential) throws AuthenticationTokenException {
        return new KubernetesAuthCertificate(credential.getClientCertificate(), credential.getClientKeySecret());
    }
}
