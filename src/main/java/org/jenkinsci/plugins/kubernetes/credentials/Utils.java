package org.jenkinsci.plugins.kubernetes.credentials;

import jenkins.security.FIPS140;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public abstract class Utils {

    /**
     * Error message used to indicate that skipping TLS verification is not accepted in FIPS mode.
     */
    public static String FIPS140_SKIP_TLS_ERROR_MESSAGE = "Skipping TLS verification is not accepted in FIPS mode.";

    public static String wrapWithMarker(String begin, String end, String encodedBody) {
        return new StringBuilder(begin).append("\n")
            .append(encodedBody).append("\n")
            .append(end)
            .toString();
    }

    public static String wrapCertificate(String certData) {
        String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
        String END_CERTIFICATE = "-----END CERTIFICATE-----";
        if (!certData.startsWith(BEGIN_CERTIFICATE)) {
            return wrapWithMarker(BEGIN_CERTIFICATE, END_CERTIFICATE, certData);
        }
        return certData;
    }

    public static String wrapPrivateKey(String keyData) {
        String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
        String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";
        if (!keyData.startsWith(BEGIN_PRIVATE_KEY)) {
            return wrapWithMarker(BEGIN_PRIVATE_KEY, END_PRIVATE_KEY, keyData);
        }
        return keyData;
    }

    public static String encodeBase64(String s) {
        return Base64.encodeBase64String(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeCertificate(Certificate certificate) throws CertificateEncodingException {
        return encodeBase64(wrapCertificate(Base64.encodeBase64String(certificate.getEncoded())));
    }

    public static String encodeKey(Key key) {
        return encodeBase64(wrapPrivateKey(Base64.encodeBase64String(key.getEncoded())));
    }

    /**
     * Ensure that the URI request is FIPS compliant for the given HttpUriRequest object and skipTLSVerify option.
     * Throw an exception if the request is invalid.
     * A request is considered valid if there is no potential leak of credentials (setting a credentials without using TLS) and
     * if the TLS verification is not skipped.
     * If FIPS mode is not enabled, this method does nothing.
     *
     * @param uriRequest     The request to validate
     * @param skipTLSVerify  A flag indicating whether to skip TLS verification or not
     * @throws IllegalArgumentException  If the request is invalid
     */
    public static void ensureFIPSCompliantURIRequest(HttpUriRequest uriRequest, boolean skipTLSVerify) {
        if (FIPS140.useCompliantAlgorithms()) {
            boolean isHttps = uriRequest.getURI().getScheme().equals("https");
            if (!isHttps && uriRequest.containsHeader(HttpHeaders.AUTHORIZATION)) {
                throw new IllegalArgumentException("Non-TLS connection is not accepted in FIPS mode when a credential is present.");
            }
            if (isHttps && skipTLSVerify) {
                throw new IllegalArgumentException(Utils.FIPS140_SKIP_TLS_ERROR_MESSAGE);
            }
        }
    }
}
