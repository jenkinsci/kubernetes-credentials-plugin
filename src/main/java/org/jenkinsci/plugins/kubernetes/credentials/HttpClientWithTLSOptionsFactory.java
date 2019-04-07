package org.jenkinsci.plugins.kubernetes.credentials;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.HostnameVerifier;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @author Max Laverse
 **/
public class HttpClientWithTLSOptionsFactory {
    // Used to auto-accept certificate
    private static TrustStrategy ALWAYS_TRUST_CERTIFICATE = (chain, authType) -> true;

    // Disables the automatic following of redirection as this is where to tokens can be read
    private static RedirectStrategy NO_HTTP_REDIRECT = new RedirectStrategy() {
        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws
                ProtocolException {
            return false;
        }

        @Override
        public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            return null;
        }
    };

    /*
     *  Loads a certificate from a string
     */
    private static X509Certificate loadFromString(String certificate) throws CertificateException {
        CertificateFactory f = CertificateFactory.getInstance("X509");
        return (X509Certificate) f.generateCertificate(new Base64InputStream(new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8))));
    }

    /*
     *  Returns a SSLConnectionSocketFactory that will accept any certificate
     */
    private static SSLConnectionSocketFactory getAlwaysTrustSSLFactory() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final SSLContextBuilder sslBuilder = new SSLContextBuilder();
        sslBuilder.loadTrustMaterial(null, ALWAYS_TRUST_CERTIFICATE);
        return new SSLConnectionSocketFactory(sslBuilder.build(), NoopHostnameVerifier.INSTANCE);
    }

    /*
     * Returns a SSLConnectionSocketFactory that will check responses against a given certificate
     */
    private static SSLConnectionSocketFactory getVerifyCertSSLFactory(String hostname, String caCertData) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException {
        final SSLContextBuilder sslBuilder = new SSLContextBuilder();
        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null);
        ks.setCertificateEntry(hostname, loadFromString(caCertData));
        sslBuilder.loadTrustMaterial(ks, null);
        return new SSLConnectionSocketFactory(sslBuilder.build(), hostnameVerifier);
    }

    public static HttpClientBuilder getBuilder(URI uri, String caCertData, boolean skipTLSVerify) throws TLSConfigurationError {
        final HttpClientBuilder builder = HttpClients.custom().setRedirectStrategy(NO_HTTP_REDIRECT);

        try {
            if (skipTLSVerify) {
                builder.setSSLSocketFactory(getAlwaysTrustSSLFactory());
            } else if (caCertData != null) {
                builder.setSSLSocketFactory(getVerifyCertSSLFactory(uri.getHost(), caCertData));
            }
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            throw new TLSConfigurationError(e);
        }

        return builder;
    }

    public static class TLSConfigurationError extends Exception {
        public TLSConfigurationError(Exception e) {
            super(e);
        }
    }
}
