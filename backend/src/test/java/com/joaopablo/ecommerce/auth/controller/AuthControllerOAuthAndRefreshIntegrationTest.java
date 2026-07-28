package com.joaopablo.ecommerce.auth.controller;

import com.joaopablo.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.joaopablo.ecommerce.auth.entity.RefreshToken;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.repository.RefreshTokenRepository;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.auth.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.Duration;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerOAuthAndRefreshIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setup() {
        refreshTokenRepository.deleteAll();
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role customerRole = roleRepository.save(
                Role.builder()
                        .name("CUSTOMER")
                        .description("Customer role")
                        .build()
        );

        user = User.builder()
                .firstName("Joao")
                .lastName("Pablo")
                .email("oauth.refresh@email.com")
                .password(passwordEncoder.encode("Senha@123"))
                .cpf("12345678999")
                .enabled(true)
                .emailVerified(true)
                .build();

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(customerRole)
                .build();

        user.getUserRoles().add(userRole);
        customerRole.getUserRoles().add(userRole);

        user = userRepository.save(user);
    }

    @Test
    void googleEndpointShouldRedirectToOAuthAuthorization() throws Exception {
        mockMvc.perform(get("/api/v1/auth/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/google"));
    }

    @Test
    void loginShouldReturnAccessAndRefreshTokens() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "oauth.refresh@email.com",
                                  "password": "Senha@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400000));
    }

    @Test
    void refreshEndpointShouldRotateTokens() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "oauth.refresh@email.com",
                                  "password": "Senha@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String oldRefreshToken = com.jayway.jsonpath.JsonPath.read(
                loginResult.getResponse().getContentAsString(),
                "$.refreshToken"
        );

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").value(not(oldRefreshToken)))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400000));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(oldRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshEndpointShouldRejectExpiredToken() throws Exception {
        RefreshToken expired = refreshTokenRepository.save(RefreshToken.builder()
                .token("expired-refresh-token")
                .user(user)
                .expiresAt(Instant.now().minus(Duration.ofHours(1)))
                .revoked(false)
                .build());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(expired.getToken())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshEndpointShouldRejectRevokedToken() throws Exception {
        RefreshToken revoked = refreshTokenRepository.save(RefreshToken.builder()
                .token("revoked-refresh-token")
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revoked(true)
                .build());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(revoked.getToken())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshEndpointShouldRejectUnknownToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "does-not-exist"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
