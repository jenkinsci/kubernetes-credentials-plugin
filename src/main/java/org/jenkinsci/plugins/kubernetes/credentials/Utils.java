package org.jenkinsci.plugins.kubernetes.credentials;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public abstract class Utils {

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
}
