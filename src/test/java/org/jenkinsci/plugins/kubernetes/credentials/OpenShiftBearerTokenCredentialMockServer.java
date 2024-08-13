package org.jenkinsci.plugins.kubernetes.credentials;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Nevin Sunny
 * Date: 13/08/24
 * Time: 11:24 am
 */
public class OpenShiftBearerTokenCredentialMockServer {

	public static void registerHttpHandlers(HttpServer server) throws IOException {
		BiConsumer<String, HttpHandler> register = server::createContext;

		register.accept("/bad-location/oauth/authorize",
		                OpenShiftBearerTokenCredentialMockServer::badLocationHandler);
		register.accept("/missing-location/oauth/authorize", OpenShiftBearerTokenCredentialMockServer::missingLocationHandler);
		register.accept("/bad-response/oauth/authorize", OpenShiftBearerTokenCredentialMockServer::badResponseHandler);
		register.accept("/valid-response/oauth/authorize", OpenShiftBearerTokenCredentialMockServer::validResponseHandler1);
		register.accept("/valid-response2/oauth/authorize", OpenShiftBearerTokenCredentialMockServer::validResponseHandler2);
		register.accept("/", OpenShiftBearerTokenCredentialMockServer::defaultHandler);
	}

	private static void badLocationHandler(HttpExchange he) throws IOException {
		String redirectURL = "bad";
		he.getResponseHeaders().set("Location", redirectURL);
		he.sendResponseHeaders(302, -1);
	}

	private static void missingLocationHandler(HttpExchange he) throws IOException {
		he.sendResponseHeaders(302, -1); // No Location header
	}

	private static void badResponseHandler(HttpExchange he) throws IOException {
		he.sendResponseHeaders(400, -1);
	}

	private static void validResponseHandler1(HttpExchange he) throws IOException {
		String redirectURL = "http://my-service/#access_token=1234&expires_in=86400";
		he.getResponseHeaders().set("Location", redirectURL);
		he.sendResponseHeaders(302, -1);
	}

	private static void validResponseHandler2(HttpExchange he) throws IOException {
		String redirectURL = "http://my-service/#access_token=1235&expires_in=86400";
		he.getResponseHeaders().set("Location", redirectURL);
		he.sendResponseHeaders(302, -1);
	}

	private static void defaultHandler(HttpExchange he) throws IOException {
		String path = he.getRequestURI().getPath();
		Pattern pattern = Pattern.compile("(.*)/.well-known/oauth-authorization-server");
		Matcher matcher = pattern.matcher(path);

		String scheme = "http";
		if(he.getHttpContext().getServer() instanceof HttpsServer){
			scheme = "https";
		}

		String hostname = "localhost";
		int port = he.getLocalAddress().getPort();

		if (matcher.find()) {
			String responseToClient = "{\n" +
			                          "  \"issuer\": \"" + scheme + "://" + hostname + ":" + port + "/\",\n" +
			                          "  \"authorization_endpoint\": \"" + scheme + "://" + hostname + ":" + port + matcher.group(1) + "/oauth/authorize\",\n" +
			                          "  \"token_endpoint\": \"" + scheme + "://" + hostname + ":" + port + "/oauth/token\",\n" +
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

			byte[] responseBytes = responseToClient.getBytes();
			he.sendResponseHeaders(200, responseBytes.length);
			try (OutputStream os = he.getResponseBody()) {
				os.write(responseBytes);
			}
		} else {
			he.sendResponseHeaders(500, -1);
			he.getResponseBody().write(("Bad test: unknown path " + path).getBytes());
		}
	}
}
