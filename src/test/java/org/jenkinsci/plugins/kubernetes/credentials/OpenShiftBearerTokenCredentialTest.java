package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.util.Secret;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.jenkinsci.plugins.kubernetes.credentials.OpenShiftBearerTokenCredentialImpl.EARLY_EXPIRE_DELAY;
import static org.junit.Assert.assertEquals;

/**
 * @author Max Laverse
 */
public class OpenShiftBearerTokenCredentialTest {

    protected static final String CREDENTIAL_ID = "cred1234";
    protected static final String USERNAME = "max.laverse";
    protected static final String PASSWORD = "super-secret";
    protected static final String SERVER_URL = "http://127.0.0.1";

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    @Ignore
    public void testEquals() throws IOException {
        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, CREDENTIAL_ID, "sample",USERNAME,PASSWORD);
        String token = t.getToken(SERVER_URL,"TEST",true);
    }

    @Test
    public void testValidTokenExtraction() throws OpenShiftBearerTokenCredentialImpl.TokenResponseError {
        OpenShiftBearerTokenCredentialImpl.Token token = OpenShiftBearerTokenCredentialImpl.extractTokenFromLocation("https://master.cluster.local:8443/oauth/token/display#access_token=VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU&expires_in=86400&token_type=bearer");
        assertEquals("VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU",token.value);
        assertEquals(86400000,token.expire-System.currentTimeMillis()+EARLY_EXPIRE_DELAY);
    }

    @Test(expected = OpenShiftBearerTokenCredentialImpl.TokenResponseError.class)
    public void testInvalidExpireTokenExtraction() throws OpenShiftBearerTokenCredentialImpl.TokenResponseError {
        OpenShiftBearerTokenCredentialImpl.extractTokenFromLocation("https://master.cluster.local:8443/oauth/token/display#access_token=VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU&expires_in=bad&token_type=bearer");
    }

    @Test(expected = OpenShiftBearerTokenCredentialImpl.TokenResponseError.class)
    public void testErroneousTokenExtraction() throws OpenShiftBearerTokenCredentialImpl.TokenResponseError {
        OpenShiftBearerTokenCredentialImpl.extractTokenFromLocation("https://master.cluster.local:8443/oauth/token/display#error=an+error+has_occured&error_description=bad+username&access_token=VO4dAgNGLnX5MGYu_wXau8au2Rw0QAqnwq8AtrLkMfU&expires_in=86400&token_type=bearer");
    }

    @Test
    public void testAuthorizationHeader() {
        String header = OpenShiftBearerTokenCredentialImpl.getBasicAuthenticationHeader(USERNAME, Secret.fromString(PASSWORD));
        assertEquals("Basic bWF4LmxhdmVyc2U6c3VwZXItc2VjcmV0", header);
    }
}