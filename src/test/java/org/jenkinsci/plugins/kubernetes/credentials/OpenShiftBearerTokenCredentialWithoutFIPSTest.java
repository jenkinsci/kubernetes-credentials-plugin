package org.jenkinsci.plugins.kubernetes.credentials;

import hudson.util.FormValidation;
import jenkins.security.FIPS140;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.FlagRule;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;

@RunWith(Parameterized.class)
public class OpenShiftBearerTokenCredentialWithoutFIPSTest extends AbstractOpenShiftBearerTokenCredentialFIPSTest {
    @ClassRule
    public static FlagRule<String> fipsFlag = FlagRule.systemProperty(FIPS140.class.getName() + ".COMPLIANCE", "false");

    public OpenShiftBearerTokenCredentialWithoutFIPSTest(
            String scheme, boolean skipTLSVerify, boolean shouldPass, String motivation) {
        super(scheme, skipTLSVerify, shouldPass, motivation);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            // Valid use cases
            {"https", false, true, "Not in FIPS mode, any combination should be valid"},
            {"https", true, true, "Not in FIPS mode, any combination should be valid"},
            {"http", true, true, "Not in FIPS mode, any combination should be valid"},
            {"http", false, true, "Not in FIPS mode, any combination should be valid"},
        });
    }

    /**
     * similar test to {@link OpenShiftBearerTokenCredentialWithFIPSTest#tooShortPassword()} but here not in FIPS context
     * so it is accepted
     */
    @Test
    public void tooShortPasswordForFIPS() throws Exception {
        FormValidation formValidation = new OpenShiftBearerTokenCredentialImpl.DescriptorImpl().doCheckPassword("");
        assertThat(formValidation.getMessage(), nullValue());
        formValidation = new OpenShiftBearerTokenCredentialImpl.DescriptorImpl().doCheckPassword("tooshort");
        assertThat(formValidation.getMessage(), nullValue());
        formValidation = new OpenShiftBearerTokenCredentialImpl.DescriptorImpl().doCheckPassword("theaustraliancricketteamisthebest");
        assertThat(formValidation.getMessage(), nullValue());
    }
}
