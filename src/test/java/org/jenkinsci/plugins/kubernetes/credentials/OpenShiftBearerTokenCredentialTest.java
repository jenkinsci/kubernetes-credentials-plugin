package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.util.Secret;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Max Laverse
 */
public class OpenShiftBearerTokenCredentialTest {

    protected static final String CREDENTIAL_ID = "cred1234";
    protected static final String USERNAME = "max.laverse";
    protected static final String PASSWORD = "super-secret";

    private MockWebServer server;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void prepareFakeOAuthServer() throws Exception {
        server = new MockWebServer();
        MockServerDispatcher dispatcher = new MockServerDispatcher(server);
        server.setDispatcher(dispatcher);
        server.start();
    }

    @After
    public void unprepareFakeOAuthServer() throws Exception {
        server.shutdown();
    }

    @Test
    public void testValidResponse() throws IOException {
        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        String token = t.getToken(server.url("/valid-response").toString(), null, true);
        assertEquals("1234", token);
    }

    @Test
    public void testMultipleCachedTokens() throws IOException {
        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        String token1 = t.getToken(server.url("/valid-response").toString(), null, true);
        String token2 = t.getToken(server.url("/valid-response2").toString(), null, true);
        String token3 = t.getToken(server.url("/valid-response").toString(), null, true);

        assertEquals("1234", token1);
        assertEquals("1235", token2);
        assertEquals("1234", token3);
    }

    @Test
    public void testBadStatusCode() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("The response from the OAuth server was invalid: The OAuth service didn't respond with a redirection but with '400: Client Error'");

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        t.getToken(server.url("/bad-response").toString(), null, true);
    }

    @Test
    public void testMissingLocation() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("The response from the OAuth server was invalid: The OAuth service didn't respond with location header");

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        t.getToken(server.url("/missing-location").toString(), null, true);
    }

    @Test
    public void testBadLocation() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("The response from the OAuth server was invalid: The response contained no token");

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        t.getToken(server.url("/bad-location").toString(), null, true);
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