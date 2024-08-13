package org.jenkinsci.plugins.kubernetes.auth;

import static org.junit.Assert.fail;

import org.junit.Test;

public abstract class AbstractKubernetesAuthConfigFIPSTest {
    protected String scheme;

    protected boolean skipTLSVerify;

    protected boolean shouldPass;

    protected String motivation;

    public AbstractKubernetesAuthConfigFIPSTest(
            String scheme, boolean skipTLSVerify, boolean shouldPass, String motivation) {
        this.scheme = scheme;
        this.skipTLSVerify = skipTLSVerify;
        this.shouldPass = shouldPass;
        this.motivation = motivation;
    }

    @Test
    public void testCreateKubernetesAuthConfig() {
        try {
            new KubernetesAuthConfig(scheme + "://server", null, skipTLSVerify);
            if (!shouldPass) {
                fail("This test was expected to fail, reason: " + motivation);
            }
        } catch (IllegalArgumentException e) {
            if (shouldPass) {
                fail("This test was expected to pass, reason: " + motivation);
            }
        }
    }
}
