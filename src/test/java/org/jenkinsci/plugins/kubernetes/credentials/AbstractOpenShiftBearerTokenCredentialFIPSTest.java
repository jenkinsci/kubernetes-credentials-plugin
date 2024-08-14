package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.FlagRule;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import static org.junit.Assert.fail;

public abstract class AbstractOpenShiftBearerTokenCredentialFIPSTest {
    private static final URL keystore;

    @ClassRule
    public static FlagRule<String> truststoreFlag;

    @ClassRule
    public static FlagRule<String> truststorePasswordFlag;

    static {
        keystore = AbstractOpenShiftBearerTokenCredentialFIPSTest.class.getResource("keystore.jks");
        if (keystore == null) {
            fail("Unable to find keystore.jks");
        }
        truststoreFlag = FlagRule.systemProperty("javax.net.ssl.trustStore", keystore.getPath());
        truststorePasswordFlag = FlagRule.systemProperty("javax.net.ssl.trustStorePassword", "unittest");
    }

    protected String scheme;

    protected boolean skipTLSVerify;

    protected boolean shouldPass;

    protected String motivation;

    private HttpServer server;


    public AbstractOpenShiftBearerTokenCredentialFIPSTest(
            String scheme, boolean skipTLSVerify, boolean shouldPass, String motivation) {
        this.scheme = scheme;
        this.skipTLSVerify = skipTLSVerify;
        this.shouldPass = shouldPass;
        this.motivation = motivation;
    }

    @Before
    public void prepareFakeOAuthServer() throws Exception {
        if (keystore == null) {
            fail("Unable to find keystore.jks");
        }

        InetSocketAddress address = new InetSocketAddress("localhost", 0);
        if ("https".equals(scheme)) {
            server = HttpsServer.create(address, 0);
            setupHttps((HttpsServer) server);
            OpenShiftBearerTokenCredentialMockServer.registerHttpHandlers(server);
        } else {
            server = HttpServer.create(address, 0);
            OpenShiftBearerTokenCredentialMockServer.registerHttpHandlers(server);
        }

        server.start();
    }

    private void setupHttps(HttpsServer httpsServer) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream is = keystore.openStream()) {
            ks.load(is, "unittest".toCharArray());
        }
        kmf.init(ks, "unittest".toCharArray());

        sslContext.init(kmf.getKeyManagers(), null, null);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
    }

    @After
    public void unprepareFakeOAuthServer() {
        server.stop(0);
    }

    @Test
    public void ensureFIPSCompliantURIRequest() throws IOException {
        OpenShiftBearerTokenCredentialImpl cred;
        cred = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, "id", "description", "username", "password");
        try {
            cred.getToken(scheme + "://localhost:" + server.getAddress().getPort() + "/valid-response", null, skipTLSVerify);
            if (!shouldPass) {
                fail("This test was expected to fail, reason: " + motivation);
            }
        } catch (IOException e) {
            // Because of how the code is done, the IllegalArgumentException can be wrapped into multiple Exception
            // Search if one of the causes is an IllegalArgumentException
            boolean legitException = false;
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof IllegalArgumentException) {
                    if (shouldPass) {
                        fail("This test was expected to pass, reason: " + motivation);
                    }
                    // The IllegalArgumentException was expected, exit the loop now
                    legitException = true;
                    break;
                }
                cause = cause.getCause();
            }
            if(!legitException) {
                throw e;
            }
        } catch (IllegalArgumentException e) {
            if (shouldPass) {
                fail("This test was expected to pass, reason: " + motivation);
            }
        }
    }
}
