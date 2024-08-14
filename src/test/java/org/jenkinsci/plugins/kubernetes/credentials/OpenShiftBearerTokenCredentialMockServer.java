package org.jenkinsci.plugins.kubernetes.credentials;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nevin Sunny
 */
public class OpenShiftBearerTokenCredentialMockServer {

    public static void registerHttpHandlers(HttpServer server) {
        server.createContext(
                "/bad-location/oauth/authorize",
                OpenShiftBearerTokenCredentialMockServer::badLocationHandler);
        server.createContext(
                "/missing-location/oauth/authorize",
                OpenShiftBearerTokenCredentialMockServer::missingLocationHandler);
        server.createContext(
                "/bad-response/oauth/authorize",
                OpenShiftBearerTokenCredentialMockServer::badResponseHandler);
        server.createContext(
                "/valid-response/oauth/authorize",
                OpenShiftBearerTokenCredentialMockServer::validResponseHandler1);
        server.createContext(
                "/valid-response2/oauth/authorize",
                OpenShiftBearerTokenCredentialMockServer::validResponseHandler2);
        server.createContext("/", OpenShiftBearerTokenCredentialMockServer::defaultHandler);
    }

    private static void badLocationHandler(HttpExchange he) throws IOException {
        he.getResponseHeaders().set("Location", "bad");
        he.sendResponseHeaders(302, -1);
    }

    private static void missingLocationHandler(HttpExchange he) throws IOException {
        he.sendResponseHeaders(302, -1); // No Location header
    }

    private static void badResponseHandler(HttpExchange he) throws IOException {
        he.sendResponseHeaders(400, -1);
    }

    private static void validResponseHandler1(HttpExchange he) throws IOException {
        he.getResponseHeaders().set("Location", "http://my-service/#access_token=1234&expires_in=86400");
        he.sendResponseHeaders(302, -1);
    }

    private static void validResponseHandler2(HttpExchange he) throws IOException {
        he.getResponseHeaders().set("Location", "http://my-service/#access_token=1235&expires_in=86400");
        he.sendResponseHeaders(302, -1);
    }

    private static void defaultHandler(HttpExchange he) throws IOException {
        String path = he.getRequestURI().getPath();
        Pattern r = Pattern.compile("(.*)/.well-known/oauth-authorization-server");
        // Now create matcher object.
        Matcher m = r.matcher(path);
        if (m.find()) {
            String scheme = he.getHttpContext().getServer() instanceof HttpsServer ? "https" : "http";
            String rootURL = scheme + "://localhost:" + he.getLocalAddress().getPort() + "/";
            String responseToClient = "{\n" +
                    "  \"issuer\": \"" + rootURL + "\",\n" +
                    "  \"authorization_endpoint\": \"" + rootURL + "/" + m.group(1) + "/oauth/authorize\",\n" +
                    "  \"token_endpoint\": \"" + rootURL + "/oauth/token\",\n" +
                    "  \"scopes_supported\": [\n" +
                    "    \"user:check-access\",\n" +
                    "    \"user:full\",\n" +
                    "    \"user:info\",\n" +
                    "    \"user:list-projects\",\n" +
                    "    \"user:list-scoped-projects\"\n" +
                    "  ],\n" +
                    "  \"response_types_supported\": [\n" +
                    "    \"code\",\n" +
                    "    \"token\"\n" +
                    "  ],\n" +
                    "  \"grant_types_supported\": [\n" +
                    "    \"authorization_code\",\n" +
                    "    \"implicit\"\n" +
                    "  ],\n" +
                    "  \"code_challenge_methods_supported\": [\n" +
                    "    \"plain\",\n" +
                    "    \"S256\"\n" +
                    "  ]\n" +
                    "}";
            byte[] responseBytes = responseToClient.getBytes(StandardCharsets.UTF_8);
            he.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = he.getResponseBody()) {
                os.write(responseBytes);
            }
        } else {
            String responseToClient = "Bad test: unknown path " + path;
            byte[] responseBytes = responseToClient.getBytes(StandardCharsets.UTF_8);
            he.sendResponseHeaders(500, responseBytes.length);
            try (OutputStream os = he.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}
