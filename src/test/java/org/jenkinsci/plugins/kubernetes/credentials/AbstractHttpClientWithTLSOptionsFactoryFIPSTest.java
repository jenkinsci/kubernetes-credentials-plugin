package org.jenkinsci.plugins.kubernetes.credentials;

import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

public abstract class AbstractHttpClientWithTLSOptionsFactoryFIPSTest {
    protected String scheme;

    protected boolean skipTLSVerify;

    protected boolean shouldPass;

    protected String motivation;

    public AbstractHttpClientWithTLSOptionsFactoryFIPSTest(String scheme, boolean skipTLSVerify, boolean shouldPass, String motivation) {
        this.scheme = scheme;
        this.skipTLSVerify = skipTLSVerify;
        this.shouldPass = shouldPass;
        this.motivation = motivation;
    }

    @Test
    public void testCreateKubernetesAuthConfig() throws URISyntaxException {
        try {
            URI uri;
            if (scheme != null) {
                uri = new URI(scheme, "localhost", null, null);
            } else {
                uri = null;
            }
            HttpClientWithTLSOptionsFactory.getBuilder(uri, null, skipTLSVerify);
            if (!shouldPass) {
                fail("This test was expected to fail, reason: " + motivation);
            }
        } catch (IllegalArgumentException e) {
            if (shouldPass) {
                fail("This test was expected to pass, reason: " + motivation);
            }
        } catch (HttpClientWithTLSOptionsFactory.TLSConfigurationError e) {
            fail("This test should not cause a TLSConfigurationError");
        }
    }
}
