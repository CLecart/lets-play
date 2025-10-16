package com.example.lets_play.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration test to assert that the HttpsEnforcerFilter redirects HTTP requests to HTTPS
 * when the feature flag is enabled.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.security.force-https=true")
public class HttpsEnforcerFilterTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void whenForceHttpsEnabled_thenHttpRequestsAreRedirectedToHttps() throws Exception {
        // Perform a request simulating an insecure (http) request
        MvcResult result = mvc.perform(get("/api/auth/ping").header("X-Forwarded-Proto", "http"))
                .andExpect(status().isMovedPermanently())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).isNotEmpty();
        assertThat(location).startsWith("https://");
    }
}
