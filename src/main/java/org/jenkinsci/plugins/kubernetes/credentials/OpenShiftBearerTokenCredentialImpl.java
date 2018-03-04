package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.Extension;
import hudson.util.Secret;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 * @author Max Laverse
 * <p>
 * For the specification, see:
 * https://docs.openshift.com/enterprise/3.0/architecture/additional_concepts/authentication.html#oauth
 */
public class OpenShiftBearerTokenCredentialImpl extends UsernamePasswordCredentialsImpl implements TokenProducer {
    // Used to artificially reduce the lifespan of a token
    protected static final long EARLY_EXPIRE_DELAY = 100;
    private static final long serialVersionUID = 6031616605797622926L;
    private static final Logger logger = Logger.getLogger(OpenShiftBearerTokenCredentialImpl.class.getName());
    private transient AtomicReference<Token> token = new AtomicReference<Token>();

    @DataBoundConstructor
    public OpenShiftBearerTokenCredentialImpl(CredentialsScope scope, String id, String description, String username, String password) {
        super(scope, id, description, username, password);
    }

    /*
     * Extract a token from url parameters
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="SF_SWITCH_NO_DEFAULT", justification="Other values can be discarded")
    protected static Token extractTokenFromLocation(String location) throws TokenResponseError {
        String parameters = location.substring(location.indexOf('#') + 1);
        List<NameValuePair> pairs = URLEncodedUtils.parse(parameters, StandardCharsets.UTF_8);

        String error = "", errorDescription = "";
        Token token = new Token();
        for (NameValuePair pair : pairs) {
            switch (pair.getName()) {
                case "access_token":
                    token.value = pair.getValue();
                    break;
                case "expires_in":
                    try {
                        token.expire = System.currentTimeMillis() + Long.parseLong(pair.getValue()) * 1000 - EARLY_EXPIRE_DELAY;
                    } catch (NumberFormatException e) {
                        throw new TokenResponseError("Bad format for the token expiration value: " + pair.getValue());
                    }
                    break;
                case "error":
                    error = pair.getValue();
                    break;
                case "error_description":
                    errorDescription = pair.getValue();
                    break;
            }
        }
        if (!error.isEmpty() || !errorDescription.isEmpty()) {
            throw new TokenResponseError("An error was returned instead of a token: " + error + " " + errorDescription);
        }
        return token;
    }

    /*
     * Return a header value for basic authentication
     */
    protected static String getBasicAuthenticationHeader(String username, Secret password) {
        return "Basic " + Base64.encodeBase64String((username + ':' + Secret.toString(password)).getBytes(StandardCharsets.UTF_8));
    }

    private Object readResolve() {
        token = new AtomicReference<Token>();
        return this;
    }

    @Override
    /*
     * Return the previously stored Token or ask for a new one
     */
    public String getToken(String oauthServerURL, String caCertData, boolean skipTlsVerify) throws IOException {
        Token token = this.token.get();
        if (token == null || System.currentTimeMillis() > token.expire) {
            try {
                token = refreshToken(oauthServerURL, caCertData, skipTlsVerify);
            } catch (HttpClientWithTLSOptionsFactory.TLSConfigurationError e) {
                throw new IOException("Could not configure SSL Factory in HttpClientWithTLSOptionsFactory: " + e.getMessage(), e);
            } catch (URISyntaxException e) {
                throw new IOException("The OAuth server URL was invalid ('" + oauthServerURL + "'): " + e.getMessage(), e);
            } catch (TokenResponseError e) {
                throw new IOException("The response from the OAuth server was invalid: " + e.getMessage(), e);
            }

            this.token.set(token);
        }

        return token.value;
    }

    /*
     * Ask for a new token by calling the OpenShift OAuth server
     */
    private synchronized Token refreshToken(String oauthServerURL, String caCertData, boolean skipTLSVerify) throws URISyntaxException, HttpClientWithTLSOptionsFactory.TLSConfigurationError, TokenResponseError, IOException {
        URI uri = new URI(oauthServerURL);

        final HttpClientBuilder builder = HttpClientWithTLSOptionsFactory.getBuilder(uri, caCertData, skipTLSVerify);
        HttpGet authorize = new HttpGet(oauthServerURL + "/oauth/authorize?client_id=openshift-challenging-client&response_type=token");
        authorize.setHeader("Authorization", getBasicAuthenticationHeader(getUsername(), getPassword()));
        final CloseableHttpResponse response = builder.build().execute(authorize);

        if (response.getStatusLine().getStatusCode() != 302) {
            throw new TokenResponseError("The OAuth service didn't respond with a redirection but with '" + response.getStatusLine().getStatusCode() + "'");
        }

        Header location = response.getFirstHeader("Location");
        if (location==null) {
            throw new TokenResponseError("The OAuth service didn't respond with location header");
        }

        return extractTokenFromLocation(location.getValue());
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

    public static class TokenResponseError extends Exception {
        public TokenResponseError(String message) {
            super(message);
        }
    }
}
