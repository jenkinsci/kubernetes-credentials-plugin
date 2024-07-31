package org.jenkinsci.plugins.kubernetes.auth;

import jenkins.security.FIPS140;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;

/**
 * Configuration object for {@link KubernetesAuth} operations.
 */
public class KubernetesAuthConfig {
    /**
     * Server URL of the API endpoint
     */
    private final String serverUrl;
    /**
     * Server certificate
     */
    private final String caCertificate;
    /**
     * Set to true to skip TLS verification
     */
    private final boolean skipTlsVerify;

    public KubernetesAuthConfig(String serverUrl, String caCertificate, boolean skipTlsVerify) {
        if (FIPS140.useCompliantAlgorithms() && skipTlsVerify && serverUrl.startsWith("https://")) {
            throw new IllegalArgumentException(Utils.FIPS140_SKIP_TLS_ERROR_MESSAGE);
        }
        this.serverUrl = serverUrl;
        this.caCertificate = caCertificate;
        this.skipTlsVerify = skipTlsVerify;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getCaCertificate() {
        return caCertificate;
    }

    public boolean isSkipTlsVerify() {
        return skipTlsVerify;
    }
}
