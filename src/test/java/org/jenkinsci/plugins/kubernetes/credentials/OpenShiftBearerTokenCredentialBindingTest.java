package org.jenkinsci.plugins.kubernetes.credentials;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.FilePath;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.test.steps.SemaphoreStep;
import org.junit.*;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.io.IOException;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.grep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OpenShiftBearerTokenCredentialBindingTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    private Server server;
    private String serverUri;

    @Before
    public void prepareFakeOAuthServer() throws Exception {
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new MockHttpServlet()), "/*");

        server = new Server(0);
        server.setHandler(context);
        server.start();

        serverUri = server.getURI().toString();
    }

    @After
    public void unprepareFakeOAuthServer() throws Exception {
        server.stop();
    }

    @Test
    public void basics() throws IOException {
        final String credentialsId = "creds";
        final String username = "bob";
        final String password = "s$$cr3t";

        OpenShiftBearerTokenCredentialImpl t = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, credentialsId, "sample", username, password);
        String token = t.getToken(serverUri + "valid-response", null, false);

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                OpenShiftBearerTokenCredentialImpl c = new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, credentialsId, "sample", username, password);
                CredentialsProvider.lookupStores(story.j.jenkins).iterator().next().addCredentials(Domain.global(), c);

                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  withCredentials([openshiftBearerToken(oauthServerURL: '" + serverUri + "valid-response', variable: 'TOKEN', credentialsId: '" + credentialsId + "')]) {\n"
                        + "    semaphore 'basics'\n"
                        + "    sh 'echo $TOKEN'\n"
                        + "  }\n"
                        + "}", true));

                WorkflowRun b = p.scheduleBuild2(0).waitForStart();
                SemaphoreStep.waitForStart("basics/1", b);
            }
        });

        story.addStep(new Statement() {
            @Override public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.getItemByFullName("p", WorkflowJob.class);
                assertNotNull(p);

                WorkflowRun b = p.getBuildByNumber(1);
                assertNotNull(b);
                assertEquals(Collections.<String>emptySet(), grep(b.getRootDir(), password));

                SemaphoreStep.success("basics/1", null);
                story.j.waitForCompletion(b);
                story.j.assertBuildStatusSuccess(b);
                story.j.assertLogNotContains(password, b);

                FilePath script = story.j.jenkins.getWorkspaceFor(p).child("script");
                assertTrue(script.exists());
                assertEquals("echo 1234", script.readToString().trim());
                assertEquals(Collections.<String>emptySet(), grep(b.getRootDir(), password));
            }
        });
    }
}
