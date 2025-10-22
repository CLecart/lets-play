package com.example.lets_play.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpAndHttpsConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${server.port:8443}")
    private int httpsPort;

    @Value("${app.security.enable-http:true}")
    private boolean enableHttp;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        if (enableHttp) {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme("http");
            connector.setPort(8080);
            connector.setSecure(false);
            factory.addAdditionalTomcatConnectors(connector);
        }
    }
}
