package org.jenkinsci.plugins.kubernetes.credentials;

import org.junit.Test;
import java.net.URI;


public class HttpClientWithTLSOptionsFactoryTest {

    protected static final String CA_CERTIFICATE = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURCekNDQWUrZ0F3SUJBZ0lKQUlFUHN4T3RWcVdPTUEwR0NTcUdTSWIzRFFFQkJRVUFNQm94R0RBV0JnTlYKQkFNTUQzZDNkeTVsZUdGdGNHeGxMbU52YlRBZUZ3MHhPVEEwTURjeE9UQXhNVE5hRncweU9UQTBNRFF4T1RBeApNVE5hTUJveEdEQVdCZ05WQkFNTUQzZDNkeTVsZUdGdGNHeGxMbU52YlRDQ0FTSXdEUVlKS29aSWh2Y05BUUVCCkJRQURnZ0VQQURDQ0FRb0NnZ0VCQU1EL2pLaEhQNXh0b2FEZ2FZK1R5OXBuM3BwWWNJemtoQlhzR2ZHQ29iU1QKcHZlVWd6cW8xWVJpUmZPWnM2cE8rbzhWZlZoNmo0V0ZadE4rZ0tXVFBmZEp5UW5hQWNFTVRJaWFJNkQ3MnVzOQpXRTFQS0pVNWY5dGduSE1mbjlwTVZUeUE1VXRNNjdhTDBqMUZDOGZnazQ5NmUxKzBpK2Ryb0xHdXdwcFVCalNtCmhXaFV1RHovNjJ0QXJyZHk5QUh0UEhFZ2NPeUxnMW83RHovL1dEWlEwNGhKME0renhKYmZaOUorY1ZXMWE2UmIKdEEwdWNEWXBTL0NPM3pjVi9yQmF0eDdWcW1CelNXQ0VCaHVVeFJka2RNZGdmM1g4Tmt3Sm5RSTBDN25GNU5zVgpZblpOdlJaYWxtajVvUTFCalcxTmFmQmZOYzBhcTN0dm95anN3REREMC9NQ0F3RUFBYU5RTUU0d0hRWURWUjBPCkJCWUVGREs4NHl4MGhwRTlyM09kOHVkalpsMDlCeVpxTUI4R0ExVWRJd1FZTUJhQUZESzg0eXgwaHBFOXIzT2QKOHVkalpsMDlCeVpxTUF3R0ExVWRFd1FGTUFNQkFmOHdEUVlKS29aSWh2Y05BUUVGQlFBRGdnRUJBQUV1R1VOawpBbjkwL0U2RGJKK01uRERtUU11SHFtSXlBMjlrb0pyNm51ZGNBenZGZzhMU21wWi9pVUE2RmdSbjVSLzVsOTlwCkFVOVd4bWRId3pRY1ZURGFabElDRm5WaWhzZHlLd09jWGdsZ3VwWEV3Mkd1MFpTQWtrOVR5azRGZ0pHZ1RBVTIKa2xHWFFKZmlIWnZxYU9lMGhVQkxnK1ZLb3VIZTFlcFlDQ0RnYnNOTEVneHBjMjQ5OXA2RVV5VlA0MzRsWVFwdgpaSFZuR3FPZnkwd3ZoTmZic014QmJVOFJlMXF3MWw1Yk1HUElIeXVJR2lNZENJVWpkZHQrcE1mK3hBMmZUYmpwCnRWYU5oblJXYXNVWmtwUDFxWjBXeERTUDUvSDdDMnhWQzkydFdRTnFWTFRJOGVWUzkveG1icWU4V3F3Y0RCZ1EKUGVqSVhTZWtOMHNGeGdZPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==";

    @Test
    public void testIgnoreCertificateWithSkipTLSVerify() throws HttpClientWithTLSOptionsFactory.TLSConfigurationError {
        URI uri = URI.create("https://www.example.com");
        HttpClientWithTLSOptionsFactory.getBuilder(uri, "bad-certificate", true);
    }


    @Test(expected = HttpClientWithTLSOptionsFactory.TLSConfigurationError.class)
    public void testThrowAnErrorOnInvalidCertificate() throws HttpClientWithTLSOptionsFactory.TLSConfigurationError {
        URI uri = URI.create("https://www.example.com");
        HttpClientWithTLSOptionsFactory.getBuilder(uri, "bad-certificate", false);
    }

    @Test
    public void testValidCertificate() throws HttpClientWithTLSOptionsFactory.TLSConfigurationError {
        URI uri = URI.create("https://www.example.com");
        HttpClientWithTLSOptionsFactory.getBuilder(uri, CA_CERTIFICATE, false);
    }
}
