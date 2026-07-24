package com.joaopablo.ecommerce.auth.controller;

import com.joaopablo.ecommerce.auth.dto.request.CreateUserRequest;
import com.joaopablo.ecommerce.auth.dto.request.LoginRequestDTO;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.UserResponse;
import com.joaopablo.ecommerce.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            description = "Autentica usuário por email e senha e retorna token JWT.",
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
                            value = "{\n  \"token\": \"jwt_token\",\n  \"type\": \"Bearer\",\n  \"expiresIn\": 86400000,\n  \"user\": {\n    \"id\": \"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11\",\n    \"firstName\": \"Joao\",\n    \"lastName\": \"Pablo\",\n    \"email\": \"joao.pablo@email.com\",\n    \"roles\": [\"CUSTOMER\"]\n  }\n}"
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
}
