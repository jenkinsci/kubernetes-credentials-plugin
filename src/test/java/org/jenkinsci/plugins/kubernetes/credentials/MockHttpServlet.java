package org.jenkinsci.plugins.kubernetes.credentials;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
            default:
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Bad test: unknown path " + request.getPathInfo());
                break;
        }
    }
}
