package org.jenkinsci.plugins.kubernetes.auth.impl;

import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertArrayEquals;

public class KubernetesAuthKeystoreTest {

    protected static final String PASSPHRASE = "test";

    @Test
    public void createConfig() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("kubernetes.pkcs12")) {
            KeyStore keyStore = loadKeyStore(resourceAsStream, PASSPHRASE.toCharArray());
            KubernetesAuthKeystore auth = new KubernetesAuthKeystore(keyStore, PASSPHRASE);
            Config c = Serialization.yamlMapper().readValue(
                    auth.buildKubeConfig(new KubernetesAuthConfig("serverUrl", "caCertificate", false)), Config.class
            );

            // verifying class doesn't modify cert and key data, so not using here
            assertArrayEquals(readFile("test.crt"), Base64.decodeBase64(c.getUsers().get(0).getUser().getClientCertificateData()));
            assertArrayEquals(readFile("test.key"), Base64.decodeBase64(c.getUsers().get(0).getUser().getClientKeyData()));
        }
    }

    private byte[] readFile(String name) throws IOException {
        return IOUtils.toByteArray(getClass().getResourceAsStream(name));
    }

    private static KeyStore loadKeyStore(InputStream inputStream, char[] password) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(inputStream, password);
        return keyStore;
    }
}
