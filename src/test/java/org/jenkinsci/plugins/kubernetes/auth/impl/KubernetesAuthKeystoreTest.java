package org.jenkinsci.plugins.kubernetes.auth.impl;

import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertEquals;

public class KubernetesAuthKeystoreTest {

    protected static final String PASSPHRASE = "test";

    @Test
    public void createConfig() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("kubernetes.pkcs12")) {
            KeyStore keyStore = loadKeyStore(resourceAsStream, PASSPHRASE.toCharArray());
            String alias = keyStore.aliases().nextElement();
            // Get private key using passphrase
            Key key = keyStore.getKey(alias, PASSPHRASE.toCharArray());
            Certificate certificate = keyStore.getCertificate(alias);
            KubernetesAuthKeystore auth = new KubernetesAuthKeystore(keyStore, PASSPHRASE);
            Config c = Serialization.yamlMapper().readValue(
                    auth.buildKubeConfig(new KubernetesAuthConfig("serverUrl", "caCertificate", false)), Config.class
            );

            // verifying class doesn't modify cert and key data, so not using here
            assertEquals(Utils.wrapCertificate(Base64.encodeBase64String(certificate.getEncoded())), new String(Base64.decodeBase64(c.getUsers().get(0).getUser().getClientCertificateData()), StandardCharsets.UTF_8));
            assertEquals(Utils.wrapPrivateKey(Base64.encodeBase64String(key.getEncoded())), new String(Base64.decodeBase64(c.getUsers().get(0).getUser().getClientKeyData()), StandardCharsets.UTF_8));
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
