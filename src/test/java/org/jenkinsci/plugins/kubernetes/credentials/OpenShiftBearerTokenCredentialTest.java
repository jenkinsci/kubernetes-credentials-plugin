package org.jenkinsci.plugins.kubernetes.credentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.cloudbees.plugins.credentials.CredentialsScope;

import hudson.util.Secret;

/**
 * @author Max Laverse
 */
public class OpenShiftBearerTokenCredentialTest {

    protected static final String CREDENTIAL_ID = "cred1234";
    protected static final String USERNAME = "max.laverse";
    protected static final String PASSWORD = "super-secret";

    @Rule
    public JenkinsRule r = new JenkinsRule();

    private Server server;

    @Before
    public void prepareFakeOAuthServer() throws Exception {
        InetSocketAddress addr = new InetSocketAddress("localhost", 0);
        server = new Server(addr);
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
    public void testMultipleCachedTokens() throws IOException {
        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
        String token1 = t.getToken(server.getURI() + "valid-response", null, true);
        String token2 = t.getToken(server.getURI() + "valid-response2", null, true);
        String token3 = t.getToken(server.getURI() + "valid-response", null, true);
        assertEquals("1234", token1);
        assertEquals("1235", token2);
        assertEquals("1234", token3);
    }

    @Test
    public void testBadStatusCode() throws IOException {
		assertThrows("The response from the OAuth server was invalid: The OAuth service didn't respond with a redirection but with '400: Bad Request'",
				IOException.class, () -> {
			        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
			        t.getToken(server.getURI() + "bad-response", null, true);
				});
    }

    @Test
    public void testMissingLocation() throws IOException {
		assertThrows("The response from the OAuth server was invalid: The OAuth service didn't respond with location header",
				IOException.class, () -> {
			        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
			        t.getToken(server.getURI() + "missing-location", null, true);
				});
    }

	@Test
	public void testBadLocation() throws IOException {

		assertThrows("The response from the OAuth server was invalid: The response contained no token",
				IOException.class, () -> {
					OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(
							CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample", USERNAME, PASSWORD);
					t.getToken(server.getURI() + "bad-location", null, true);
				});
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
		assertThrows("Bad format for the token expiration value: bad",
				OpenShiftBearerTokenCredentialImpl.TokenResponseError.class,
				() -> OpenShiftBearerTokenCredentialImpl.extractTokenFromLocation(
						"https://master.cluster.local:8443/oauth/token/display#access_token=VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU&expires_in=bad&token_type=bearer"));
	}

	@Test
	public void testErroneousTokenExtraction() throws OpenShiftBearerTokenCredentialImpl.TokenResponseError {
		assertThrows("An error was returned instead of a token: an error has_occured, bad username",
				OpenShiftBearerTokenCredentialImpl.TokenResponseError.class,
				() -> OpenShiftBearerTokenCredentialImpl.extractTokenFromLocation(
						"https://master.cluster.local:8443/oauth/token/display#error=an+error+has_occured&error_description=bad+username&access_token=VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU&expires_in=86400&token_type=bearer"));
	}

    @Test
    public void testAuthorizationHeader() {
        String header = OpenShiftBearerTokenCredentialImpl.getBasicAuthenticationHeader(USERNAME, Secret.fromString(PASSWORD));
        assertEquals("Basic bWF4LmxhdmVyc2U6c3VwZXItc2VjcmV0", header);
    }
}