package org.jenkinsci.plugins.kubernetes.credentials;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class MockServerDispatcher extends Dispatcher {

	private final MockWebServer server;

	public MockServerDispatcher(MockWebServer server) {
		this.server = server;
	}

	@Override
	public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
		String path = request.getRequestUrl().encodedPath();

		switch (path) {
			case "/bad-location/oauth/authorize":
				return new MockResponse()
						.setResponseCode(302)
						.setHeader("Location", "bad");

			case "/missing-location/oauth/authorize":
				return new MockResponse()
						.setResponseCode(302); // No Location header

			case "/bad-response/oauth/authorize":
				return new MockResponse()
						.setResponseCode(400);

			case "/valid-response/oauth/authorize":
				return new MockResponse()
						.setResponseCode(302)
						.setHeader("Location", "http://my-service/#access_token=1234&expires_in=86400");

			case "/valid-response2/oauth/authorize":
				return new MockResponse()
						.setResponseCode(302)
						.setHeader("Location", "http://my-service/#access_token=1235&expires_in=86400");

			default:
				Pattern pattern = Pattern.compile("(.*)/.well-known/oauth-authorization-server");
				Matcher matcher = pattern.matcher(path);

				if (matcher.find()) {
					String responseToClient = "{\n" +
					                          "  \"issuer\": \"" + server.url("/").toString() + "\",\n" +
					                          "  \"authorization_endpoint\": \"" + server.url("/") + matcher.group(1) + "/oauth/authorize\",\n" +
					                          "  \"token_endpoint\": \"" + server.url("/oauth/token") + "\",\n" +
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
					return new MockResponse()
							.setResponseCode(200)
							.setBody(responseToClient);
			}

			return new MockResponse()
					.setResponseCode(500)
					.setBody("Bad test: unknown path " + path);
		}
	}
}
