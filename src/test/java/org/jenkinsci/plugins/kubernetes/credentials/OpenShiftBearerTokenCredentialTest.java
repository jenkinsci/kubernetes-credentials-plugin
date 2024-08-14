package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.sun.net.httpserver.HttpServer;
import hudson.util.Secret;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Max Laverse
 */
public class OpenShiftBearerTokenCredentialTest {

    protected static final String CREDENTIAL_ID = "cred1234";
    protected static final String USERNAME = "max.laverse";
    protected static final String PASSWORD = "super-secret";

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private HttpServer server;

    @Before
    public void prepareFakeOAuthServer() throws Exception {
        InetSocketAddress address = new InetSocketAddress("localhost", 0);
        server = HttpServer.create(address, 0);
        OpenShiftBearerTokenCredentialMockServer.registerHttpHandlers(server);
        server.start();
    }

    @After
    public void unprepareFakeOAuthServer() {
        server.stop(0);
    }

    private String getURI() {
        InetSocketAddress address = server.getAddress();
        return "http://" + address.getHostString() + ":" + address.getPort() + "/";
    }

    @Test
    public void testValidResponse() throws IOException {
        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        String token = t.getToken(getURI() + "valid-response", null, true);
        assertEquals("1234", token);
    }

    @Test
    public void testMultipleCachedTokens() throws IOException {
        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        String token1 = t.getToken(getURI() + "valid-response", null, true);
        String token2 = t.getToken(getURI() + "valid-response2", null, true);
        String token3 = t.getToken(getURI() + "valid-response", null, true);
        assertEquals("1234", token1);
        assertEquals("1235", token2);
        assertEquals("1234", token3);
    }

    @Test
    public void testBadStatusCode() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("The response from the OAuth server was invalid: The OAuth service didn't respond with a redirection but with '400: Bad Request'");

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        t.getToken(getURI() + "bad-response", null, true);
    }

    @Test
    public void testMissingLocation() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("The response from the OAuth server was invalid: The OAuth service didn't respond with location header");

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        t.getToken(getURI() + "missing-location", null, true);
    }

    @Test
    public void testBadLocation() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("The response from the OAuth server was invalid: The response contained no token");

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        t.getToken(getURI() + "bad-location", null, true);
    }

    @Test
    public void testTokenExtractionEarlyExpire() throws OpenShiftBearerTokenCredentialImpl.TokenResponseError {
        long expectedExpirySec = 86400 - OpenShiftBearerTokenCredentialImpl.EARLY_EXPIRE_DELAY_SEC;
        OpenShiftBearerTokenCredentialImpl.Token token = OpenShiftBearerTokenCredentialImpl.extractTokenFromLocation("https://master.cluster.local:8443/oauth/token/display#access_token=VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU&expires_in=86400&token_type=bearer");
        assertEquals("VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU", token.value);

        long expirySec = (token.expire - System.currentTimeMillis())/1000;

        // We want to test if the expiration time of the token takes EARLY_EXPIRE_DELAY_SEC into account.
        // Since there can be additional delays caused by the testing infrastructure, we check if
        // the token expiration is close to the expected value with a 10 seconds margin.
        assertTrue(expectedExpirySec-10 <= expirySec && expirySec <= expectedExpirySec);
    }

    @Test
    public void testInvalidExpireTokenExtraction() throws OpenShiftBearerTokenCredentialImpl.TokenResponseError {
        expectedEx.expect(OpenShiftBearerTokenCredentialImpl.TokenResponseError.class);
        expectedEx.expectMessage("Bad format for the token expiration value: bad");

        OpenShiftBearerTokenCredentialImpl.extractTokenFromLocation("https://master.cluster.local:8443/oauth/token/display#access_token=VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU&expires_in=bad&token_type=bearer");
    }

    @Test
    public void testErroneousTokenExtraction() throws OpenShiftBearerTokenCredentialImpl.TokenResponseError {
        expectedEx.expect(OpenShiftBearerTokenCredentialImpl.TokenResponseError.class);
        expectedEx.expectMessage("An error was returned instead of a token: an error has_occured, bad username");

        OpenShiftBearerTokenCredentialImpl.extractTokenFromLocation("https://master.cluster.local:8443/oauth/token/display#error=an+error+has_occured&error_description=bad+username&access_token=VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU&expires_in=86400&token_type=bearer");
    }

    @Test
    public void testAuthorizationHeader() {
        String header = OpenShiftBearerTokenCredentialImpl.getBasicAuthenticationHeader(USERNAME, Secret.fromString(PASSWORD));
        assertEquals("Basic bWF4LmxhdmVyc2U6c3VwZXItc2VjcmV0", header);
    }
}