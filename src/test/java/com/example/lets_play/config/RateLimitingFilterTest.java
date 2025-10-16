package com.example.lets_play.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test to exercise the RateLimitingFilter behavior. Sets a low
 * requests-per-minute threshold and verifies the filter returns 429 after
 * the threshold is exceeded for the same client IP + URI.
 */
@SpringBootTest
@Import(RateLimitingFilterTest.TestConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.rate-limit.requests-per-minute=2")
public class RateLimitingFilterTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void whenRequestsExceedLimit_thenReturn429() throws Exception {
    final var request = get("/api/auth/test-ping").header("X-Forwarded-For", "1.2.3.4");

        // First two requests should succeed (below or at limit)
        mvc.perform(request).andExpect(status().isOk());
        mvc.perform(request).andExpect(status().isOk());

        // Third request should be rejected with 429 and a short message
        mvc.perform(request)
            .andExpect(status().isTooManyRequests())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Rate limit exceeded")));
    }

    /**
     * Simple test controller registered in the test context. This avoids
     * depending on existing application routes and keeps the test focused on
     * the filter behavior under test.
     */
    @RestController
    static class TestController {

        @GetMapping("/api/auth/test-ping")
        public String ping() {
            return "pong";
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public TestController testController() {
            return new TestController();
        }
    }
}
