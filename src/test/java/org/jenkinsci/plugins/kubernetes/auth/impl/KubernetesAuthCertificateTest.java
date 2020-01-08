package org.jenkinsci.plugins.kubernetes.auth.impl;

import io.fabric8.kubernetes.client.utils.Serialization;
import org.jenkinsci.plugins.kubernetes.auth.KubernetesAuthConfig;
import org.jenkinsci.plugins.kubernetes.auth.impl.KubernetesAuthCertificate;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KubernetesAuthCertificateTest {
    @Test
    public void createConfig() throws Exception {
        String cert_data = Utils.wrapCertificate("cert_data");
        String key_data = Utils.wrapPrivateKey("key_data");
        KubernetesAuthCertificate b = new KubernetesAuthCertificate(
                cert_data,
                key_data
        );
        io.fabric8.kubernetes.api.model.Config c = Serialization.yamlMapper().readValue(
                b.buildKubeConfig(new KubernetesAuthConfig("serverUrl", "caCertificate", false)), io.fabric8.kubernetes.api.model.Config.class
        );

        // verifying class doesn't modify cert and key data, so not using here
        assertEquals(Utils.encodeBase64(cert_data), c.getUsers().get(0).getUser().getClientCertificateData());
        assertEquals(Utils.encodeBase64(key_data), c.getUsers().get(0).getUser().getClientKeyData());
    }
}
