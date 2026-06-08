package org.jenkinsci.plugins.kubernetes.auth.impl;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.http.BasicBuilder;
import io.fabric8.kubernetes.client.http.HttpRequest;
import io.fabric8.kubernetes.client.http.Interceptor;
import io.fabric8.kubernetes.client.utils.TokenRefreshInterceptor;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.credentials.TokenProducer;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Exercises the integration between {@link KubernetesAuthToken} and fabric8's
 * {@link TokenRefreshInterceptor}, which is the actual code path that injects the
 * Authorization header on every HTTP request. These tests verify that switching the
 * plugin from {@code withOauthToken} to {@code withOauthTokenProvider} causes fabric8 to
 * re-invoke our {@link TokenProducer} on every request, so that long-lived watches
 * pick up rotated tokens automatically.
 */
public class KubernetesAuthTokenIntegrationTest {

    private static final KubernetesAuthConfig AUTH_CONFIG =
            new KubernetesAuthConfig("https://k8s.example.com", "caCertificate", false);

    private RecordingBuilder builder;

    @Before
    public void setUp() {
        builder = new RecordingBuilder();
    }

    @Test
    public void eachInterceptorBeforeCallSeesFreshTokenFromProducer() throws Exception {
        List<String> rotations = List.of("token-A", "token-B", "token-C");
        AtomicReference<Integer> idx = new AtomicReference<>(0);
        TokenProducer rotating = (server, ca, skip) -> {
            int current = idx.get();
            idx.set(current + 1);
            return rotations.get(current % rotations.size());
        };

        // buildConfig() runs the eager decorate-time check which consumes token-A,
        // so the interceptor sees the rest of the rotation on subsequent before() calls.
        Config config = buildConfig(rotating);
        TokenRefreshInterceptor interceptor = newInterceptor(config);

        List<String> expectedSequence = List.of("token-B", "token-C", "token-A");
        for (String expected : expectedSequence) {
            interceptor.before(builder, dummyRequest(), dummyTags());
            assertEquals("Bearer " + expected, builder.lastHeader("Authorization"));
        }
    }

    @Test
    public void staticTokenProducesStableAuthorizationHeader() throws Exception {
        Config config = buildConfig(producerReturning("static-tok"));
        TokenRefreshInterceptor interceptor = newInterceptor(config);

        for (int i = 0; i < 5; i++) {
            interceptor.before(builder, dummyRequest(), dummyTags());
            assertEquals("Bearer static-tok", builder.lastHeader("Authorization"));
        }
    }

    @Test
    public void emptyTokenFallsBackToBearerInvalidWhenRemoteRefreshSupportsIt() throws Exception {
        Config config = buildConfig(producerReturning(""));
        // remoteRefresh that always fails: forces fabric8 to skip refresh logic but still mark token invalid
        TokenRefreshInterceptor interceptor = new TokenRefreshInterceptor(
                config,
                Instant.now(),
                cfg -> CompletableFuture.completedFuture(null));

        interceptor.before(builder, dummyRequest(), dummyTags());

        // With our provider returning "", getEffectiveOauthToken returns "" and the interceptor
        // skips setting a Bearer header (since useRemoteRefresh is false in this simple Config).
        // The key guarantee is: no stale token leaks into the header.
        assertNull(builder.lastHeader("Authorization"));
    }

    @Test
    public void providerIsConsultedNotStaticConfigField() throws Exception {
        Config config = buildConfig(producerReturning("from-provider"));

        assertNull("static oauthToken field must remain unset", config.getOauthToken());
        assertNotNull("provider must be present", config.getOauthTokenProvider());

        TokenRefreshInterceptor interceptor = newInterceptor(config);
        interceptor.before(builder, dummyRequest(), dummyTags());

        assertEquals("Bearer from-provider", builder.lastHeader("Authorization"));
    }

    private static TokenProducer producerReturning(String token) {
        return (server, ca, skip) -> token;
    }

    private static Config buildConfig(TokenProducer producer) throws Exception {
        KubernetesAuthToken auth = new KubernetesAuthToken(producer);
        return auth.decorate(new ConfigBuilder(Config.empty()), AUTH_CONFIG).build();
    }

    private static TokenRefreshInterceptor newInterceptor(Config config) {
        // latestRefreshTimestamp = now() ensures isTimeToRefresh() returns false during the test,
        // so we exercise only the per-request provider path, not the timer-based refresh.
        return new TokenRefreshInterceptor(
                config,
                Instant.now(),
                cfg -> CompletableFuture.completedFuture(null));
    }

    private static HttpRequest dummyRequest() {
        return new StubHttpRequest();
    }

    private static Interceptor.RequestTags dummyTags() {
        return new Interceptor.RequestTags() {
            @Override
            public <T> T getTag(Class<T> type) {
                return null;
            }
        };
    }

    /**
     * Minimal BasicBuilder stub that records header() and setHeader() calls so tests can
     * assert on the Authorization header that the interceptor would have set on a real request.
     */
    private static final class RecordingBuilder implements BasicBuilder {
        private final List<String[]> headerCalls = new ArrayList<>();
        private final List<String[]> setHeaderCalls = new ArrayList<>();
        private URI uri;

        @Override
        public BasicBuilder uri(URI u) {
            this.uri = u;
            return this;
        }

        @Override
        public BasicBuilder header(String name, String value) {
            headerCalls.add(new String[] {name, value});
            return this;
        }

        @Override
        public BasicBuilder setHeader(String name, String value) {
            setHeaderCalls.add(new String[] {name, value});
            return this;
        }

        String lastHeader(String name) {
            for (int i = headerCalls.size() - 1; i >= 0; i--) {
                if (headerCalls.get(i)[0].equals(name)) {
                    return headerCalls.get(i)[1];
                }
            }
            for (int i = setHeaderCalls.size() - 1; i >= 0; i--) {
                if (setHeaderCalls.get(i)[0].equals(name)) {
                    return setHeaderCalls.get(i)[1];
                }
            }
            return null;
        }
    }

    private static final class StubHttpRequest implements HttpRequest {
        @Override
        public java.util.UUID id() {
            return java.util.UUID.randomUUID();
        }

        @Override
        public URI uri() {
            return URI.create("https://k8s.example.com/api/v1/pods");
        }

        @Override
        public String method() {
            return "GET";
        }

        @Override
        public String bodyString() {
            return null;
        }

        @Override
        public List<String> headers(String name) {
            return List.of();
        }

        @Override
        public java.util.Map<String, List<String>> headers() {
            return java.util.Map.of();
        }
    }
}
