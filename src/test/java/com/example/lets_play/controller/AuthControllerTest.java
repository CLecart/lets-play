package com.example.lets_play.controller;

import com.example.lets_play.dto.JwtResponse;
import com.example.lets_play.dto.LoginRequest;
import com.example.lets_play.security.AppUserPrincipal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerTest {

    @Test
    void authenticateUser_returnsJwtResponse_onSuccess() throws Exception {
        AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);
        AppUserPrincipal principal = Mockito.mock(AppUserPrincipal.class);
        Mockito.when(principal.getUsername()).thenReturn("user@example.com");
        Mockito.when(principal.getId()).thenReturn("abc123");
        Mockito.when(principal.getName()).thenReturn("Test User");

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getPrincipal()).thenReturn(principal);

        Mockito.when(authManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        // We only need a minimal JwtUtils stub
        var jwtUtils = new com.example.lets_play.security.JwtUtils();
        // inject a short-lived secret and expiration to avoid NPEs
        java.lang.reflect.Field secret = com.example.lets_play.security.JwtUtils.class.getDeclaredField("jwtSecret");
        secret.setAccessible(true);
        secret.set(jwtUtils, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        java.lang.reflect.Field exp = com.example.lets_play.security.JwtUtils.class.getDeclaredField("jwtExpirationMs");
        exp.setAccessible(true);
        exp.setInt(jwtUtils, 3600000);

        AuthController controller = new AuthController();
        // inject fields
        java.lang.reflect.Field am = AuthController.class.getDeclaredField("authenticationManager");
        am.setAccessible(true);
        am.set(controller, authManager);
        java.lang.reflect.Field ju = AuthController.class.getDeclaredField("jwtUtils");
        ju.setAccessible(true);
        ju.set(controller, jwtUtils);

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("password123");

        ResponseEntity<?> resp = controller.authenticateUser(req);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isInstanceOf(JwtResponse.class);
    }
}
