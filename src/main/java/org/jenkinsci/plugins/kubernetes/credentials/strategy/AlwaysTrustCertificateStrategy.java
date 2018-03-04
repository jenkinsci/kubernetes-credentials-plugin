package org.jenkinsci.plugins.kubernetes.credentials.strategy;

import org.apache.http.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author Max Laverse
 */
public class AlwaysTrustCertificateStrategy implements TrustStrategy {
    @Override
    public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        return true;
    }
}
