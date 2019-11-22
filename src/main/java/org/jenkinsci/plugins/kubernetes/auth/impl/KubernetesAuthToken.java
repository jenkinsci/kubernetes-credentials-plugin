package org.jenkinsci.plugins.kubernetes.auth.impl;

import io.fabric8.kubernetes.api.model.AuthInfoBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuth;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthException;
import org.jenkinsci.plugins.kubernetes.credentials.TokenProducer;

import java.io.IOException;

/**
 * Kubernetes authentication using a token.
 * The token itself is obtained from a {@link TokenProducer} that may need some execution context.
 */
public class KubernetesAuthToken extends AbstractKubernetesAuth implements KubernetesAuth {
    private final TokenProducer tokenProducer;

    public KubernetesAuthToken(TokenProducer tokenProducer) {
        this.tokenProducer = tokenProducer;
    }

    protected String getToken(KubernetesAuthConfig config) throws KubernetesAuthException {
        try {
            return tokenProducer.getToken(config.getServerUrl(), config.getCaCertificate(), config.isSkipTlsVerify());
        } catch (IOException e) {
            throw new KubernetesAuthException(e);
        }
    }

    @Override
    public AuthInfoBuilder decorate(AuthInfoBuilder authInfoBuilder, KubernetesAuthConfig config) throws KubernetesAuthException {
        return authInfoBuilder.withToken(getToken(config));
    }

    @Override
    public ConfigBuilder decorate(ConfigBuilder builder, KubernetesAuthConfig config) throws KubernetesAuthException {
        return builder.withOauthToken(getToken(config));
    }
}
