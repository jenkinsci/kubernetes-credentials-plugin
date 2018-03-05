package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.util.Secret;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

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

    private Server server;

    @Before
    public void prepareFakeOAuthServer() throws Exception {
        server = new Server(0);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new MockHttpServlet()), "/*");
        server.setHandler(context);
        server.start();
    }

    @After
    public void unprepareFakeOAuthServer() throws Exception {
        server.stop();
    }

    @Test
    public void testValidResponse() throws IOException {
        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        String token = t.getToken(server.getURI() + "valid-response", null, true);
        assertEquals("1234", token);
    }

    @Test
    public void testBadStatusCode() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("The response from the OAuth server was invalid: The OAuth service didn't respond with a redirection but with '400: Bad Request'");

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        t.getToken(server.getURI() + "bad-response", null, true);
    }

    @Test
    public void testMissingLocation() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("The response from the OAuth server was invalid: The OAuth service didn't respond with location header");

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        t.getToken(server.getURI() + "missing-location", null, true);
    }

    @Test
    public void testBadLocation() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("The response from the OAuth server was invalid: The response contained no token");

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        t.getToken(server.getURI() + "bad-location", null, true);
    }

    @Test
    public void testValidTokenExtraction() throws OpenShiftBearerTokenCredentialImpl.TokenResponseError {
        OpenShiftBearerTokenCredentialImpl.Token token = OpenShiftBearerTokenCredentialImpl.extractTokenFromLocation("https://master.cluster.local:8443/oauth/token/display#access_token=VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU&expires_in=86400&token_type=bearer");
        assertEquals("VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU", token.value);

        // We are optimistic here and expect the test to run in less than a second.
        // TODO: Improve
        assertEquals(86100000, token.expire - System.currentTimeMillis());
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