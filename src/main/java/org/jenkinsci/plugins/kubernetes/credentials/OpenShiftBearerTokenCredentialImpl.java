package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.Extension;
import hudson.util.Secret;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.http.NameValuePair;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.jenkinsci.plugins.kubernetes.credentials.strategy.AlwaysTrustCertificateStrategy;
import org.jenkinsci.plugins.kubernetes.credentials.strategy.NoHTTPRedirectStrategy;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.net.ssl.HostnameVerifier;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 * @author Max Laverse
 *
 * For the specification, see:
 * https://docs.openshift.com/enterprise/3.0/architecture/additional_concepts/authentication.html#oauth
 */
public class OpenShiftBearerTokenCredentialImpl extends UsernamePasswordCredentialsImpl implements TokenProducer {

    private static final long serialVersionUID = 6031616605797622926L;
    private static TrustStrategy ALWAYS_TRUST_CERTIFICATE = new AlwaysTrustCertificateStrategy();
    private static RedirectStrategy NO_HTTP_REDIRECT = new NoHTTPRedirectStrategy();
    private transient AtomicReference<Token> token = new AtomicReference<Token>();
    protected static final long EARLY_EXPIRE_DELAY = 100;

    @DataBoundConstructor
    public OpenShiftBearerTokenCredentialImpl(CredentialsScope scope, String id, String description, String username, String password) {
        super(scope, id, description, username, password);
    }

    private Object readResolve() {
        token = new AtomicReference<Token>();
        return this;
    }

    @Override
    public String getToken(String oauthServerURL, String caCertData, boolean skipTlsVerify) throws IOException {
        Token token = this.token.get();
        if (token == null || System.currentTimeMillis() > token.expire) {
            token = refreshToken(oauthServerURL, caCertData, skipTlsVerify);
            this.token.set(token);
        }

        return token.value;
    }

    private synchronized Token refreshToken(String oauthServerURL, String caCertData, boolean skipTLSVerify) throws IOException {

        URI uri = null;
        try {
            uri = new URI(oauthServerURL);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid server URL " + oauthServerURL, e);
        }

        final HttpClientBuilder builder = getHttpClientBuilder(uri, caCertData, skipTLSVerify);


        HttpGet authorize = new HttpGet(oauthServerURL + "/oauth/authorize?client_id=openshift-challenging-client&response_type=token");
        authorize.setHeader("Authorization", getAuthorizationHeader(getUsername(), getPassword()));
        final CloseableHttpResponse response = builder.build().execute(authorize);

        if (response.getStatusLine().getStatusCode() != 302) {
            throw new IOException("Failed to get an OAuth access token " + response.getStatusLine().getStatusCode());
        }

        String location = response.getFirstHeader("Location").getValue();
        return extractToken(location);
    }

    protected static Token extractToken(String location){
        String parameters = location.substring(location.indexOf('#') + 1);
        List<NameValuePair> pairs = URLEncodedUtils.parse(parameters, StandardCharsets.UTF_8);
        Token token = new Token();
        for (NameValuePair pair : pairs) {
            switch(pair.getName()){
                case "access_token":
                    token.value = pair.getValue();
                    break;
                case "expires_in":
                    token.expire = System.currentTimeMillis() + Long.parseLong(pair.getValue()) * 1000 - EARLY_EXPIRE_DELAY;
                    break;
            }
        }
        return token;
    }

    private static HttpClientBuilder getHttpClientBuilder(URI uri, String caCertData, boolean skipTLSVerify) throws IOException {
        final HttpClientBuilder builder = HttpClients.custom().setRedirectStrategy(NO_HTTP_REDIRECT);

        if (skipTLSVerify || caCertData != null) {
            final SSLContextBuilder sslBuilder = new SSLContextBuilder();
            HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();
            try {
                if (skipTLSVerify) {
                    sslBuilder.loadTrustMaterial(null, ALWAYS_TRUST_CERTIFICATE);
                    hostnameVerifier = NoopHostnameVerifier.INSTANCE;
                } else if (caCertData != null) {
                    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                    ks.load(null);
                    CertificateFactory f = CertificateFactory.getInstance("X509");
                    X509Certificate cert = (X509Certificate) f.generateCertificate(new Base64InputStream(
                            new ByteArrayInputStream(caCertData.getBytes(StandardCharsets.UTF_8))));
                    ks.setCertificateEntry(uri.getHost(), cert);
                    sslBuilder.loadTrustMaterial(ks, null);
                }

                builder.setSSLSocketFactory(new SSLConnectionSocketFactory(sslBuilder.build(), hostnameVerifier));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder;
    }

    private static String getAuthorizationHeader(String username, Secret password){
        return "Basic " + Base64.encodeBase64String((username + ':' + Secret.toString(password)).getBytes(StandardCharsets.UTF_8));
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return "OpenShift Username and Password";
        }
    }

    public static class Token {
        String value;
        long expire;
    }
}
