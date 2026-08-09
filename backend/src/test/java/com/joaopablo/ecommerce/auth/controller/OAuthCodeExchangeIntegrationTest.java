package com.joaopablo.ecommerce.auth.controller;

import com.joaopablo.ecommerce.auth.dto.internal.OAuthLoginCodePayload;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.repository.OAuthLoginCodeRedisRepository;
import com.joaopablo.ecommerce.auth.repository.RefreshTokenRepository;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.auth.repository.UserRoleRepository;
import com.joaopablo.ecommerce.auth.service.OAuthLoginCodeService;
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

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class OAuthCodeExchangeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OAuthLoginCodeService oAuthLoginCodeService;

    @Autowired
    private OAuthLoginCodeRedisRepository oAuthLoginCodeRedisRepository;

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

    private User customer;
    private User admin;

    @BeforeEach
    void setup() {
        refreshTokenRepository.deleteAll();
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role customerRole = roleRepository.save(Role.builder()
                .name("CUSTOMER").description("Customer").build());
        Role adminRole = roleRepository.save(Role.builder()
                .name("ADMIN").description("Admin").build());

        customer = persistUser("oauth.customer@email.com", "11111111111", customerRole);
        admin = persistUser("oauth.admin@email.com", "22222222222", adminRole);
    }

    @Test
    void exchange_validCode_returnsTokensAndUserIdOnce() throws Exception {
        String code = oAuthLoginCodeService.issue(loginFor(customer, List.of("CUSTOMER")));

        mockMvc.perform(post("/api/v1/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(customer.getId().toString()))
                .andExpect(jsonPath("$.user.email").value("oauth.customer@email.com"))
                .andExpect(jsonPath("$.user.roles", hasItem("CUSTOMER")));

        mockMvc.perform(post("/api/v1/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exchange_unknownCode_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"this-code-does-not-exist\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exchange_blankCode_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exchange_expiredCode_returnsUnauthorized() throws Exception {
        String code = "expired-" + UUID.randomUUID();
        oAuthLoginCodeRedisRepository.save(
                code,
                OAuthLoginCodePayload.builder()
                        .accessToken("t")
                        .refreshToken("r")
                        .type("Bearer")
                        .expiresIn(1)
                        .userId(customer.getId())
                        .email(customer.getEmail())
                        .roles(List.of("CUSTOMER"))
                        .build(),
                Duration.ofMillis(1)
        );
        Thread.sleep(50);

        mockMvc.perform(post("/api/v1/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exchange_adminPreservesAdminRole() throws Exception {
        String code = oAuthLoginCodeService.issue(loginFor(admin, List.of("ADMIN")));

        mockMvc.perform(post("/api/v1/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(admin.getId().toString()))
                .andExpect(jsonPath("$.user.roles", hasItem("ADMIN")));
    }

    @Test
    void exchange_withoutCode_cannotObtainTokens() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void refresh_stillWorksAfterPasswordLogin() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"oauth.customer@email.com","password":"Senha@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String refreshToken = com.jayway.jsonpath.JsonPath.read(
                login.getResponse().getContentAsString(), "$.refreshToken");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken", not(refreshToken)));
    }

    private User persistUser(String email, String cpf, Role role) {
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password(passwordEncoder.encode("Senha@123"))
                .cpf(cpf)
                .enabled(true)
                .emailVerified(true)
                .build();
        UserRole ur = UserRole.builder().user(user).role(role).build();
        user.getUserRoles().add(ur);
        role.getUserRoles().add(ur);
        return userRepository.save(user);
    }

    private static LoginResponseDTO loginFor(User user, List<String> roles) {
        return LoginResponseDTO.builder()
                .token("jwt-for-" + user.getId())
                .refreshToken("refresh-for-" + user.getId())
                .type("Bearer")
                .expiresIn(86400000)
                .user(LoginResponseDTO.UserLoginResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .roles(roles)
                        .build())
                .build();
    }
}
