package org.jenkinsci.plugins.kubernetes.credentials;

import jenkins.security.FIPS140;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.FlagRule;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class OpenShiftBearerTokenCredentialWithFIPSTest extends AbstractOpenShiftBearerTokenCredentialFIPSTest {
    @ClassRule
    public static FlagRule<String> fipsFlag = FlagRule.systemProperty(FIPS140.class.getName() + ".COMPLIANCE", "true");

    public OpenShiftBearerTokenCredentialWithFIPSTest(
            String scheme, boolean skipTLSVerify, boolean shouldPass, String motivation) {
        super(scheme, skipTLSVerify, shouldPass, motivation);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            // Valid use cases
            {"https", false, true, "TLS is used and the TLS verification is not skipped, this should be accepted"},
            // Invalid use cases
            {"https", true, false, "Skip TLS check is not accepted in FIPS mode"},
            {"http", false, false, "TLS is mandatory when in FIPS mode"},
            {"http", true, false, "TLS and TLS check are mandatory when in FIPS mode"},
        });
    }
}
