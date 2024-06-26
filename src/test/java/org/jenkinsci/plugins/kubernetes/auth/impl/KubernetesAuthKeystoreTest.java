package org.jenkinsci.plugins.kubernetes.auth.impl;

import hudson.util.Secret;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertEquals;

public class KubernetesAuthKeystoreTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    protected static final String PASSPHRASE = "test";

    @Test
    public void createConfig() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("kubernetes.pkcs12")) {
            KeyStore keyStore = loadKeyStore(resourceAsStream, PASSPHRASE.toCharArray());
            KubernetesAuthKeystore auth = new KubernetesAuthKeystore(keyStore, Secret.fromString(PASSPHRASE));
            Config c = Serialization.yamlMapper().readValue(
                    auth.buildKubeConfig(new KubernetesAuthConfig("serverUrl", "caCertificate", false)), Config.class
            );

            // verifying class doesn't modify cert and key data, so not using here
            assertEquals(Utils.encodeBase64(readFile("test.crt")), c.getUsers().get(0).getUser().getClientCertificateData());
            assertEquals(Utils.encodeBase64(readFile("test.key")), c.getUsers().get(0).getUser().getClientKeyData());
        }
    }

    private String readFile(String name) throws IOException {
        // Replacement is needed for getting the right content on Windows platform
        return new String(IOUtils.toByteArray(getClass().getResourceAsStream(name))).replaceAll("\\r\\n", "\n");
    }

    private static KeyStore loadKeyStore(InputStream inputStream, char[] password) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(inputStream, password);
        return keyStore;
    }
}
