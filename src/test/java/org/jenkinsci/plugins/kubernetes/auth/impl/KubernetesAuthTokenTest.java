package org.jenkinsci.plugins.kubernetes.auth.impl;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.OAuthTokenProvider;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.credentials.TokenProducer;
import org.junit.Test;

import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class KubernetesAuthTokenTest {

    private static final KubernetesAuthConfig CONFIG =
            new KubernetesAuthConfig("https://k8s.example.com", "caCertificate", false);

    @Test
    public void decorateRegistersProviderAndLeavesStaticTokenNull() throws Exception {
        KubernetesAuthToken auth = new KubernetesAuthToken(producerReturning("tok-1"));
        Config built = auth.decorate(new ConfigBuilder(Config.empty()), CONFIG).build();

        assertNull("Static oauthToken must not be set when using the provider path", built.getOauthToken());
        assertNotNull("Provider must be registered on the Config", built.getOauthTokenProvider());
        assertEquals("tok-1", built.getOauthTokenProvider().getToken());
    }

    @Test
    public void providerReinvokesProducerOnEveryCall() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        TokenProducer producer = (server, ca, skip) -> "tok-" + calls.incrementAndGet();

        // decorate() makes one eager call (tok-1) and returns the provider.
        OAuthTokenProvider provider = decorateAndGetProvider(producer);

        assertEquals("tok-2", provider.getToken());
        assertEquals("tok-3", provider.getToken());
        assertEquals("tok-4", provider.getToken());
        assertEquals(4, calls.get());
    }

    @Test
    public void rotatingProducerYieldsRotatedTokens() throws Exception {
        List<String> rotations = List.of("rot-a", "rot-b", "rot-c");
        AtomicInteger idx = new AtomicInteger();
        TokenProducer producer = (server, ca, skip) -> rotations.get(idx.getAndIncrement() % rotations.size());

        // decorate() makes one eager call which consumes rot-a; the provider returns the rest of the rotation.
        OAuthTokenProvider provider = decorateAndGetProvider(producer);

        assertEquals("rot-b", provider.getToken());
        assertEquals("rot-c", provider.getToken());
        assertEquals("rot-a", provider.getToken());
        assertEquals("rot-b", provider.getToken());
    }

    @Test
    public void staticStringCredentialReturnsSameValue() throws Exception {
        OAuthTokenProvider provider = decorateAndGetProvider(producerReturning("static-token"));

        for (int i = 0; i < 10; i++) {
            assertEquals("static-token", provider.getToken());
        }
    }

    @Test
    public void nullTokenIsReturnedAsIs() throws Exception {
        OAuthTokenProvider provider = decorateAndGetProvider(producerReturning(null));

        assertNull(provider.getToken());
    }

    @Test
    public void emptyTokenIsReturnedAsIs() throws Exception {
        OAuthTokenProvider provider = decorateAndGetProvider(producerReturning(""));

        assertEquals("", provider.getToken());
    }

    @Test
    public void producerFailureAtDecorateTimeSurfacesAsCheckedKubernetesAuthException() {
        IOException cause = new IOException("auth server unreachable");
        TokenProducer producer = (server, ca, skip) -> {
            throw cause;
        };

        KubernetesAuthToken auth = new KubernetesAuthToken(producer);

        try {
            auth.decorate(new ConfigBuilder(Config.empty()), CONFIG);
            fail("expected eager KubernetesAuthException from decorate()");
        } catch (KubernetesAuthException e) {
            assertEquals(cause, e.getCause());
        }
    }

    @Test
    public void producerFailureAfterDecorateSurfacesFromProviderAsRuntimeException() throws Exception {
        IOException cause = new IOException("refresh server unreachable");
        AtomicInteger calls = new AtomicInteger();
        TokenProducer producer = (server, ca, skip) -> {
            if (calls.getAndIncrement() == 0) {
                return "initial-token";
            }
            throw cause;
        };

        OAuthTokenProvider provider = decorateAndGetProvider(producer);
        // The eager call inside decorate() consumed call #0; subsequent provider calls fail.
        try {
            provider.getToken();
            fail("expected RuntimeException wrapping KubernetesAuthException");
        } catch (RuntimeException e) {
            assertTrue("cause should be KubernetesAuthException, was " + e.getCause(),
                    e.getCause() instanceof KubernetesAuthException);
            assertEquals(cause, e.getCause().getCause());
        }
    }

    @Test
    public void producerReceivesServerUrlAndCaAndSkipFlag() throws Exception {
        CopyOnWriteArrayList<String[]> received = new CopyOnWriteArrayList<>();
        TokenProducer producer = (server, ca, skip) -> {
            received.add(new String[] {server, ca, Boolean.toString(skip)});
            return "ok";
        };

        OAuthTokenProvider provider = decorateAndGetProvider(producer,
                new KubernetesAuthConfig("https://api.k8s/", "the-ca", true));

        provider.getToken();

        // decorate() makes one eager call, provider.getToken() makes another. Both see the same config.
        assertEquals(2, received.size());
        for (String[] entry : received) {
            assertEquals("https://api.k8s/", entry[0]);
            assertEquals("the-ca", entry[1]);
            assertEquals("true", entry[2]);
        }
    }

    @Test
    public void providerIsThreadSafeUnderConcurrentGetToken() throws Exception {
        int threads = 32;
        int callsPerThread = 50;
        AtomicInteger calls = new AtomicInteger();
        TokenProducer producer = (server, ca, skip) -> "tok-" + calls.incrementAndGet();

        OAuthTokenProvider provider = decorateAndGetProvider(producer);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch start = new CountDownLatch(1);
            CompletableFuture<?>[] futures = new CompletableFuture<?>[threads];
            for (int t = 0; t < threads; t++) {
                futures[t] = CompletableFuture.runAsync(() -> {
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                    for (int i = 0; i < callsPerThread; i++) {
                        String t1 = provider.getToken();
                        assertNotNull(t1);
                        assertTrue(t1.startsWith("tok-"));
                    }
                }, pool);
            }
            start.countDown();
            for (CompletableFuture<?> f : futures) {
                try {
                    f.get(30, TimeUnit.SECONDS);
                } catch (ExecutionException ee) {
                    throw new AssertionError(ee.getCause());
                }
            }
        } finally {
            pool.shutdownNow();
        }
        // +1 for the eager call made inside decorate().
        assertEquals(threads * callsPerThread + 1, calls.get());
    }

    private static TokenProducer producerReturning(String token) {
        return (server, ca, skip) -> token;
    }

    private static OAuthTokenProvider decorateAndGetProvider(TokenProducer producer) throws Exception {
        return decorateAndGetProvider(producer, CONFIG);
    }

    private static OAuthTokenProvider decorateAndGetProvider(TokenProducer producer, KubernetesAuthConfig config) throws Exception {
        KubernetesAuthToken auth = new KubernetesAuthToken(producer);
        Config built = auth.decorate(new ConfigBuilder(Config.empty()), config).build();
        OAuthTokenProvider provider = built.getOauthTokenProvider();
        assertNotNull(provider);
        return provider;
    }
}
