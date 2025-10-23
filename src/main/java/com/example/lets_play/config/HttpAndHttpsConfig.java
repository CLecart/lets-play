package com.example.lets_play.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * Adds an optional plain-HTTP connector next to HTTPS.
 *
 * HTTP is controlled by the property {@code app.security.enable-http}.
 * The HTTP port is configurable with {@code app.http.port} and defaults
 * to 0 (ephemeral) so tests using {@code server.port=0} won't collide.
 */
@Component
public final class HttpAndHttpsConfig
        implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    /** Whether to enable a plain HTTP connector in addition to HTTPS. */
    @Value("${app.security.enable-http:true}")
    private boolean enableHttp;

    /** Port for the optional HTTP connector. Defaults to 0 (ephemeral). */
    @Value("${app.http.port:0}")
    private int httpPort;

    @Override
    public void customize(final TomcatServletWebServerFactory factory) {
        if (!enableHttp) {
            return;
        }

        final Connector connector = new Connector(
                TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(httpPort);
        factory.addAdditionalTomcatConnectors(connector);
    }
}
