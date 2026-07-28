package com.joaopablo.ecommerce.auth.controller;

import com.joaopablo.ecommerce.auth.config.SecurityConfig;
import com.joaopablo.ecommerce.auth.security.OAuth2SuccessHandler;
import com.joaopablo.ecommerce.auth.service.AuthService;
import com.joaopablo.ecommerce.auth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void googleEndpointShouldRedirectToOAuthAuthorization() throws Exception {
        mockMvc.perform(get("/api/v1/auth/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/google"));
    }


    @Test
    void refreshEndpointShouldReturnRotatedTokens() throws Exception {

        when(authService.refresh(any()))
                .thenReturn(
                        com.joaopablo.ecommerce.auth.dto.response.TokenRefreshResponseDTO.builder()
                                .accessToken("access")
                                .refreshToken("refresh")
                                .type("Bearer")
                                .expiresIn(86400000)
                                .build()
                );


        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "old-refresh"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400000));
    }


    @Test
    void logoutEndpointShouldReturnNoContent() throws Exception {

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "valid-refresh-token"
                                }
                                """))
                .andExpect(status().isNoContent());
    }
}