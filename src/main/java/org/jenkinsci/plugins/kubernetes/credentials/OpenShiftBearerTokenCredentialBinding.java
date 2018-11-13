package org.jenkinsci.plugins.kubernetes.credentials;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.credentialsbinding.Binding;
import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class OpenShiftBearerTokenCredentialBinding extends Binding<OpenShiftBearerTokenCredentialImpl> {

    public String oauthServerURL;
    public String caCertData;
    public boolean skipTlsVerify;

    @DataBoundConstructor
    public OpenShiftBearerTokenCredentialBinding(String variable, String credentialsId) {
        super(variable, credentialsId);
    }

    @Override protected Class<OpenShiftBearerTokenCredentialImpl> type() {
        return OpenShiftBearerTokenCredentialImpl.class;
    }

    @DataBoundSetter
    public void setOauthServerURL(@Nonnull final String oauthServerURL) {
        this.oauthServerURL = oauthServerURL;
    }

    public String getOauthServerURL() {
        return oauthServerURL;
    }

    @DataBoundSetter
    public void setCaCertData(@Nonnull final String caCertData) {
        this.caCertData = caCertData;
    }

    public String getCaCertData() {
        return caCertData;
    }

    @DataBoundSetter
    public void setSkipTlsVerify(@Nonnull final boolean skipTlsVerify) {
        this.skipTlsVerify = skipTlsVerify;
    }

    public boolean getSkipTlsVerify() {
        return skipTlsVerify;
    }

    @Override public SingleEnvironment bindSingle(@Nonnull Run<?,?> build,
                                                  @Nullable FilePath workspace,
                                                  @Nullable Launcher launcher,
                                                  @Nonnull TaskListener listener) throws IOException, InterruptedException {
        OpenShiftBearerTokenCredentialImpl credentials = getCredentials(build);
        String token = credentials.getToken(getOauthServerURL(), getCaCertData(), getSkipTlsVerify());

        return new SingleEnvironment(token);
    }

    @Symbol("openshiftBearerToken")
    @Extension
    public static class DescriptorImpl extends BindingDescriptor<OpenShiftBearerTokenCredentialImpl> {

        @Override protected Class<OpenShiftBearerTokenCredentialImpl> type() {
            return OpenShiftBearerTokenCredentialImpl.class;
        }

        @Override public String getDisplayName() {
            return "OpenShift Bearer Token";
        }

        @Override public boolean requiresWorkspace() {
            return true;
        }
    }
}