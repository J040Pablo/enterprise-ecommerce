package com.joaopablo.ecommerce.auth.controller;

import com.joaopablo.ecommerce.auth.dto.request.CreateUserRequest;
import com.joaopablo.ecommerce.auth.dto.request.LoginRequestDTO;
import com.joaopablo.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.TokenRefreshResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.UserResponse;
import com.joaopablo.ecommerce.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login de usuário",
            description = "Autentica usuário por email e senha e retorna Access Token e Refresh Token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Login request",
                        value = "{\n  \"email\": \"joao.pablo@email.com\",\n  \"password\": \"Senha@123\"\n}"
                    )
                )
            ),
            responses = {
                @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = LoginResponseDTO.class),
                        examples = @ExampleObject(
                            name = "Login response",
                            value = "{\n  \"token\": \"jwt_token\",\n  \"refreshToken\": \"refresh_token\",\n  \"type\": \"Bearer\",\n  \"expiresIn\": 86400000,\n  \"user\": {\n    \"id\": \"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11\",\n    \"firstName\": \"Joao\",\n    \"lastName\": \"Pablo\",\n    \"email\": \"joao.pablo@email.com\",\n    \"roles\": [\"CUSTOMER\"]\n  }\n}"
                        )
                    )
                ),
                @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
            }
    )
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Renovar tokens",
            description = "Recebe um refresh token válido, revoga o anterior e emite novos access e refresh tokens (rotação)."
    )
    public ResponseEntity<TokenRefreshResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO request
    ) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout de usuário",
            description = "Revoga o Refresh Token do usuário impedindo renovação de Access Tokens (RFC 7009)."
    )
    public ResponseEntity<Void> logout(
            @Valid @RequestBody com.joaopablo.ecommerce.auth.dto.request.LogoutRequestDTO request
    ) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/google")
    @Operation(
            summary = "Login com Google",
            description = "Redireciona para o fluxo OAuth2 do Google."
    )
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }
}
