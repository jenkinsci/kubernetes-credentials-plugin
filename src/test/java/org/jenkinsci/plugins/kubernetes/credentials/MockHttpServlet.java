package org.jenkinsci.plugins.kubernetes.credentials;

import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Max Laverse
 */
public class MockHttpServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        switch (request.getPathInfo()) {
            case "/bad-location/oauth/authorize":
                response.sendRedirect("bad");
                break;
            case "/missing-location/oauth/authorize":
                response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                break;
            case "/bad-response/oauth/authorize":
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                break;
            case "/valid-response/oauth/authorize":
                response.sendRedirect("http://my-service/#access_token=1234&expires_in=86400");
                break;
            case "/valid-response2/oauth/authorize":
                response.sendRedirect("http://my-service/#access_token=1235&expires_in=86400");
                break;
            default:
                Pattern r = Pattern.compile("(.*)/.well-known/oauth-authorization-server");
                // Now create matcher object.
                Matcher m = r.matcher(request.getPathInfo());
                if (m.find()) {
	                String rootUrl = getRootURL(request);;

	                String responseToClient = "{\n" +
                            "  \"issuer\": \"" + rootUrl + "\",\n" +
                            "  \"authorization_endpoint\": \"" + rootUrl + "/" + m.group(1) + "/oauth/authorize\",\n" +
                            "  \"token_endpoint\": \"" + rootUrl + "/oauth/token\",\n" +
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
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(responseToClient);
                    response.getWriter().flush();
                    response.getWriter().close();
                    return;
                }
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Bad test: unknown path " + request.getPathInfo());
                break;
        }
    }

    public String getRootURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        return scheme + "://" + serverName + ":" + serverPort;
    }
}
