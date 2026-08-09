package com.joaopablo.ecommerce.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Outside {@code dev}, Swagger/OpenAPI must be denied by SecurityConfig (not merely hidden).
 * Anonymous callers receive 401 (AuthenticationEntryPoint); authenticated callers receive 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerUiIsDeniedForAnonymousOutsideDev() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiDocsAreDeniedForAnonymousOutsideDev() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void swaggerUiIsForbiddenEvenForAuthenticatedAdminOutsideDev() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void apiDocsAreForbiddenEvenForAuthenticatedAdminOutsideDev() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isForbidden());
    }
}
