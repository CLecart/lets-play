package com.example.lets_play.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.security.force-https=true"})
public class HttpsEnforcerIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void httpRequestIsRedirectedToHttps() throws Exception {
        final String url = "http://localhost:" + port + "/api/auth/ping";

    final URL u = java.net.URI.create(url).toURL();
    final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("GET");
        conn.connect();

        final int code = conn.getResponseCode();
        assertThat(code / 100).isEqualTo(3);

        final String location = conn.getHeaderField("Location");
        assertThat(location).isNotNull();
        assertThat(location).startsWith("https://");
        assertThat(location).contains("//localhost");

        conn.disconnect();
    }
}
