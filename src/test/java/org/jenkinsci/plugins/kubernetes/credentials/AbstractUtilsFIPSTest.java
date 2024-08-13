package org.jenkinsci.plugins.kubernetes.credentials;

import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;

public abstract class AbstractUtilsFIPSTest {
    protected String scheme;

    protected boolean skipTLSVerify;

    protected boolean shouldPass;

    protected String motivation;

    public AbstractUtilsFIPSTest(
            String scheme, boolean skipTLSVerify, boolean shouldPass, String motivation) {
        this.scheme = scheme;
        this.skipTLSVerify = skipTLSVerify;
        this.shouldPass = shouldPass;
        this.motivation = motivation;
    }

    @Test
    public void ensureFIPSCompliantURIRequest() throws URISyntaxException {
        try {
            if(scheme != null) {
                HttpUriRequest request = new HttpGet(new URI(scheme, "localhost", null, null));
                URI uri = request.getURI();
                Utils.ensureFIPSCompliantURIRequest(uri, skipTLSVerify);
            } else {
                Utils.ensureFIPSCompliantURIRequest(null, skipTLSVerify);
            }
            if (!shouldPass) {
                fail("This test was expected to fail, reason: " + motivation);
            }
        } catch (IllegalArgumentException e) {
            if (shouldPass) {
                fail("This test was expected to pass, reason: " + motivation);
            }
        }
    }

    @Test
    public void ensureFIPSCompliantRequest() {
        try {
            if(scheme != null) {
                Utils.ensureFIPSCompliantRequest(scheme + "://localhost", skipTLSVerify);
            } else {
                Utils.ensureFIPSCompliantRequest(null, skipTLSVerify);
            }
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
