package com.example.lets_play;

import com.example.lets_play.dto.ProductRequest;
import com.example.lets_play.dto.LoginRequest;
import com.example.lets_play.dto.UserCreateRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ForbiddenIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl;

    private RestTemplate rest = new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    private String signupAndSignin(String email) {
        // make email unique to avoid conflicts when tests run repeatedly against the same DB
        String uniqueEmail = email.replace("@", "+" + System.currentTimeMillis() + "@");

        UserCreateRequest signup = new UserCreateRequest();
        signup.setName("Test");
        signup.setEmail(uniqueEmail);
        signup.setPassword("TestPass123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserCreateRequest> signupReq = new HttpEntity<>(signup, headers);
        rest.postForEntity(baseUrl + "/auth/signup", signupReq, Map.class);

    LoginRequest login = new LoginRequest();
    login.setEmail(uniqueEmail);
        login.setPassword("TestPass123");
        HttpEntity<LoginRequest> loginReq = new HttpEntity<>(login, headers);
        ResponseEntity<String> resp = rest.postForEntity(baseUrl + "/auth/signin", loginReq, String.class);
        try {
            Map<String, Object> body = mapper.readValue(resp.getBody(), new TypeReference<>() {});
            return (String) body.get("token");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void nonOwnerUpdateShouldReturn403() {
        // create user A and sign in
        String tokenA = signupAndSignin("owner@example.com");

        // create product as user A
        ProductRequest product = new ProductRequest();
        product.setName("OwnerProduct");
        product.setDescription("desc");
        product.setPrice(9.99);

        HttpHeaders authA = new HttpHeaders();
        authA.setContentType(MediaType.APPLICATION_JSON);
        authA.setBearerAuth(tokenA);
        HttpEntity<ProductRequest> createReq = new HttpEntity<>(product, authA);
        ResponseEntity<String> createResp = rest.postForEntity(baseUrl + "/products", createReq, String.class);
        Assertions.assertEquals(HttpStatus.OK, createResp.getStatusCode());
        try {
            Map<String, Object> created = mapper.readValue(createResp.getBody(), new TypeReference<>() {});
            String productId = (String) created.get("id");
            // proceed to attempt update
            // create user B and sign in
            String tokenB = signupAndSignin("intruder@example.com");

            // attempt update as user B
        ProductRequest update = new ProductRequest();
        update.setName("HackedName");
        // include price so validation passes and authorization is evaluated
        update.setPrice(19.99);

            HttpHeaders authB = new HttpHeaders();
            authB.setContentType(MediaType.APPLICATION_JSON);
            authB.setBearerAuth(tokenB);
            HttpEntity<ProductRequest> updateReq = new HttpEntity<>(update, authB);

            try {
                ResponseEntity<String> updateResp = rest.exchange(baseUrl + "/products/" + productId, HttpMethod.PUT, updateReq, String.class);
                // If we get here, the request did not fail as expected
                Assertions.fail("Expected 403 Forbidden but got " + updateResp.getStatusCode());
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                Assertions.assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
