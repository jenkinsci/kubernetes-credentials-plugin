package org.jenkinsci.plugins.kubernetes.credentials;

import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;

public abstract class AbstractUtilsFIPSTest {
    protected String scheme;

    protected boolean auth;

    protected boolean skipTLSVerify;

    protected boolean shouldPass;

    protected String motivation;

    public AbstractUtilsFIPSTest(
            String scheme, boolean auth, boolean skipTLSVerify, boolean shouldPass, String motivation) {
        this.scheme = scheme;
        this.auth = auth;
        this.skipTLSVerify = skipTLSVerify;
        this.shouldPass = shouldPass;
        this.motivation = motivation;
    }

    @Test
    public void ensureFIPSCompliantURIRequest() throws URISyntaxException {
        HttpUriRequest request = new HttpGet(new URI(scheme, "localhost", null, null));
        if (auth) {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Basic xyz");
        }
        try {
            Utils.ensureFIPSCompliantURIRequest(request, skipTLSVerify);
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
