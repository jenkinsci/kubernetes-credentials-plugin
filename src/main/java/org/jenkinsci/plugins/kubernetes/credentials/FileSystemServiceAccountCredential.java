package org.jenkinsci.plugins.kubernetes.credentials;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.util.Secret;

/**
 * Read the OAuth bearer token from service account file provisioned by kubernetes
 * <a href="http://kubernetes.io/v1.0/docs/admin/service-accounts-admin.html">Service Account Admission Controller</a>
 * when Jenkins itself is deployed inside a Pod.
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class FileSystemServiceAccountCredential extends BaseStandardCredentials
        implements TokenProducer, StringCredentials {

    private static final long serialVersionUID = -2222994164073756041L;

    private static final String SERVICEACCOUNT_TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    @DataBoundConstructor
    public FileSystemServiceAccountCredential(CredentialsScope scope, String id, String description) {
        super(scope, id, description);
    }

    @Override
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    public Secret getSecret() {
        try {
            return Secret.fromString(FileUtils.readFileToString(new File(SERVICEACCOUNT_TOKEN_PATH)));
        } catch (IOException e) {
            return Secret.fromString(null);
        }
    }

    @Override
    @Deprecated
    public String getToken(String serviceAddress, String caCertData, boolean skipTlsVerify) {
        return getSecret().getPlainText();
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
        @Override
        public boolean isApplicable(CredentialsProvider provider) {
            return new File(SERVICEACCOUNT_TOKEN_PATH).exists();
        }

        @Override
        public String getDisplayName() {
            return "Kubernetes Service Account";
        }
    }

}
