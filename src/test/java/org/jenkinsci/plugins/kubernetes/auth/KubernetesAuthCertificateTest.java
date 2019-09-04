package org.jenkinsci.plugins.kubernetes.auth;

import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.jenkinsci.plugins.kubernetes.credentials.Utils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class KubernetesAuthCertificateTest {
    @Test
    public void createConfig() throws IOException {
        KubernetesAuthCertificate b = new KubernetesAuthCertificate(
                Utils.wrapCertificate("cert_data"),
                Utils.wrapPrivateKey("key_data")
        );
        Config c = Serialization.yamlMapper().readValue(
                b.buildKubeConfig("serverUrl", "caCertificate"), Config.class
        );

        // verifying class doesn't modify cert and key data, so not using  here
        assertEquals(Utils.wrapCertificate("cert_data"), c.getUsers().get(0).getUser().getClientCertificateData());
        assertEquals(Utils.wrapPrivateKey("key_data"), c.getUsers().get(0).getUser().getClientKeyData());
    }
}
