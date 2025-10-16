package com.example.lets_play.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication entry point used to return a compact, standardized JSON
 * response for unauthorized requests.
 *
 * <p>When authentication fails or a request is unauthorized, this component
 * sends a JSON response containing status, error, message and path to the
 * client. It centralizes unauthorized response handling for the API.</p>
 *
 * <p><strong>Best practices:</strong> Avoid exposing internal details in the
 * message field to prevent information leakage. Use logging for detailed
 * diagnostics instead.</p>
 *
 * @since 1.0
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    /** Logger for authentication entry point errors and diagnostics. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
        AuthEntryPointJwt.class);

    /**
     * Handles unauthorized requests by writing a compact JSON response to
     * the servlet response stream.
     *
     * <p>This method is an extension point for custom handling of
     * authentication failures. Subclasses that override it must preserve the
     * contract: set the response status to {@code 401} for unauthorized
     * requests and avoid exposing internal details in the response body.
     * Implementations that wish to keep the default JSON body may call
     * {@code super.commence(request, response, authException);}.</p>
     *
     * @param request the incoming HTTP servlet request
     * @param response the HTTP servlet response to write to
     * @param authException the authentication exception that caused this
     *                      invocation
     * @throws IOException when an I/O error occurs while writing the body
     * @throws ServletException when a servlet error occurs
     */
    @Override
    public void commence(final HttpServletRequest request,
                         final HttpServletResponse response,
                         final AuthenticationException authException)
            throws IOException, ServletException {
        LOGGER.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
