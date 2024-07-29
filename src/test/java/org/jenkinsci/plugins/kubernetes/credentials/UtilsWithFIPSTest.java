package org.jenkinsci.plugins.kubernetes.credentials;

import java.util.Arrays;
import java.util.Collection;
import jenkins.security.FIPS140;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.FlagRule;

@RunWith(Parameterized.class)
public class UtilsWithFIPSTest extends AbstractUtilsFIPSTest {
    @ClassRule
    public static FlagRule<String> fipsFlag = FlagRule.systemProperty(FIPS140.class.getName() + ".COMPLIANCE", "true");

    public UtilsWithFIPSTest(
            String scheme, boolean auth, boolean skipTLSVerify, boolean shouldPass, String motivation) {
        super(scheme, auth, skipTLSVerify, shouldPass, motivation);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            // Valid use cases
            {"https", true, false, true, "TLS is used and the TLS verification is not skipped, this should be accepted"},
            {"https", false, false, true, "TLS is used and the TLS verification is not skipped, this should be accepted"},
            {"http", false, false, true, "No credentials is used, this should be accepted in FIPS mode"},
            {"http", false, true, true, "No credentials is used, this should be accepted in FIPS mode"},
            // Invalid use cases
            {"https", true, true, false, "Skip TLS check is not accepted in FIPS mode"},
            {"https", false, true, false, "Skip TLS check is not accepted in FIPS mode"},
            {"http", true, true, false, "Credentials must be used in combination with TLS when in FIPS mode"},
            {"http", true, false, false, "Credentials must be used in combination with TLS when in FIPS mode"},
        });
    }
}
