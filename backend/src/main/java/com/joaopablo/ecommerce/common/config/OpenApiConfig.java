package com.joaopablo.ecommerce.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-commerce API")
                        .version("1.0.0")
                        .description("""
                                REST API developed with Spring Boot featuring:
                                - JWT Authentication
                                - Google OAuth2 Login
                                - Product Management
                                - Inventory Control
                                - Shopping Cart
                                - Order Processing
                                - Payment Management
                                - Shipping Management
                                """)
                        .contact(new Contact()
                                .name("João Pablo"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "JWT Bearer Token. Obtain a token via POST /api/v1/auth/login " +
                                                "and provide it here as: Bearer <token>")));
    }
}
