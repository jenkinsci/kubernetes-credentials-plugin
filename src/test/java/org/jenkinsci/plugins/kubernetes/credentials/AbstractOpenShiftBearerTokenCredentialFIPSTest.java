package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.FlagRule;

import java.io.IOException;
import java.net.URL;

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

    private Server server;

    private ServerConnector sslConnector;

    private ServerConnector serverConnector;


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
        server = new Server();

        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystore.toExternalForm());
        sslContextFactory.setKeyManagerPassword("unittest");
        sslContextFactory.setKeyStorePassword("unittest");

        sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(httpsConfig));
        serverConnector = new ServerConnector(server);
        server.setConnectors(new Connector[]{serverConnector, sslConnector});

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
    public void ensureFIPSCompliantURIRequest() throws IOException {
        OpenShiftBearerTokenCredentialImpl cred;
        cred = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, "id", "description", "username", "password");
        try {
            int port;
            if ("https".equals(scheme)) {
                port = sslConnector.getLocalPort();
            } else {
                port = serverConnector.getLocalPort();
            }
            cred.getToken(scheme + "://localhost:" + port + "/valid-response", null, skipTLSVerify);
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
