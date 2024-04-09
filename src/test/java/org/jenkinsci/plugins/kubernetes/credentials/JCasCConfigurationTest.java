package org.jenkinsci.plugins.kubernetes.credentials;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.List;

import org.jvnet.hudson.test.RestartableJenkinsRule;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;

import io.jenkins.plugins.casc.misc.RoundTripAbstractTest;
import jenkins.model.Jenkins;

public class JCasCConfigurationTest extends RoundTripAbstractTest {

    @Override
    protected void assertConfiguredAsExpected(RestartableJenkinsRule restartableJenkinsRule, String s) {
        /*******
         * The FileSystemServiceAccountCredential is only created when running inside a Kubernetes Pod.
         ******/
        //        List<FileSystemServiceAccountCredential> creds =
        //                CredentialsProvider.lookupCredentials(FileSystemServiceAccountCredential.class,
        //                        Jenkins.getInstanceOrNull(), null, Collections.emptyList());
        //        assertThat(creds.size(), is(1));
        //        FileSystemServiceAccountCredential fileSystemServiceAccountCredential = creds.get(0);
        //        assertThat(fileSystemServiceAccountCredential.getId(), is("aa0fa603-3f73-4a1e-98dc-b3a236cc83be"));
        //        assertThat(fileSystemServiceAccountCredential.getScope(), is(CredentialsScope.GLOBAL));

        List<OpenShiftBearerTokenCredentialImpl> creds1 =
                CredentialsProvider.lookupCredentialsInItemGroup(OpenShiftBearerTokenCredentialImpl.class,
                        Jenkins.getInstanceOrNull(), null, Collections.emptyList());
        assertThat(creds1.size(), is(1));
        OpenShiftBearerTokenCredentialImpl openShiftBearerTokenCredential = creds1.get(0);
        assertThat(openShiftBearerTokenCredential.getUsername(), is("foobar"));
        assertThat(openShiftBearerTokenCredential.getPassword().getPlainText(), is("bazqux"));
        assertThat(openShiftBearerTokenCredential.getId(), is("ocp-cred"));
        assertThat(openShiftBearerTokenCredential.getDescription(), is("OCP u/p cred"));
        assertThat(openShiftBearerTokenCredential.getScope(), is(CredentialsScope.GLOBAL));
    }

    @Override
    protected String stringInLogExpected() {
        return "OpenShiftBearerTokenCredentialImpl#password";
    }
}
