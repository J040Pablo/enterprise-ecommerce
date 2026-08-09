package com.joaopablo.ecommerce.auth.controller;

import com.joaopablo.ecommerce.auth.dto.request.CreateUserRequest;
import com.joaopablo.ecommerce.auth.dto.request.LoginRequestDTO;
import com.joaopablo.ecommerce.auth.dto.request.LogoutRequestDTO;
import com.joaopablo.ecommerce.auth.dto.request.OAuthCodeExchangeRequest;
import com.joaopablo.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.TokenRefreshResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.UserResponse;
import com.joaopablo.ecommerce.auth.service.AuthService;
import com.joaopablo.ecommerce.auth.service.OAuthLoginCodeService;
import com.joaopablo.ecommerce.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Authentication",
        description = "User registration, login, token lifecycle management and Google OAuth2 integration"
)
public class AuthController {

    private final AuthService authService;
    private final OAuthLoginCodeService oAuthLoginCodeService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new customer account. Email and CPF must be unique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Registered user",
                                    value = """
                                            {
                                              "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                                              "firstName": "João",
                                              "lastName": "Pablo",
                                              "email": "joao.pablo@email.com",
                                              "cpf": "123.456.789-00",
                                              "phone": "+55 11 99999-9999",
                                              "enabled": true,
                                              "emailVerified": false
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error — invalid or missing fields",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict — email or CPF already registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user with email and password. Returns a JWT access token and a refresh token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Login request",
                        value = """
                                {
                                  "email": "joao.pablo@email.com",
                                  "password": "Senha@123"
                                }"""
                    )
                )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful — JWT tokens returned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Login response",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiJ9...",
                                              "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
                                              "type": "Bearer",
                                              "expiresIn": 86400000,
                                              "user": {
                                                "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                                                "firstName": "João",
                                                "lastName": "Pablo",
                                                "email": "joao.pablo@email.com",
                                                "roles": ["CUSTOMER"]
                                              }
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error — invalid or missing fields",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — invalid email or password",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh JWT tokens",
            description = "Accepts a valid refresh token, revokes it and issues a new pair of access and refresh tokens (token rotation)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens refreshed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenRefreshResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error — refresh token is blank or missing",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — refresh token is invalid or expired",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<TokenRefreshResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO request
    ) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = """
                    Revokes the user's refresh token, preventing further access token renewals (RFC 7009).

                    **Note:** This endpoint requires a valid `refreshToken` in the request body.
                    It does **not** use the `Authorization: Bearer` header.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful — refresh token revoked"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error — refresh token is blank or missing",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequestDTO request
    ) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/oauth/exchange")
    @Operation(
            summary = "Exchange OAuth login code for tokens",
            description = """
                    Exchanges a one-time opaque code (from the Google OAuth frontend callback)
                    for access and refresh tokens. The code is single-use and short-lived.
                    Tokens are never returned via redirect URL.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens issued successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error — code is blank or missing",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — code is invalid, expired, or already used",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<LoginResponseDTO> exchangeOAuthCode(
            @Valid @RequestBody OAuthCodeExchangeRequest request
    ) {
        return ResponseEntity.ok(oAuthLoginCodeService.exchange(request.getCode()));
    }

    @GetMapping("/google")
    @Operation(
            summary = "Initiate Google OAuth2 Login",
            description = """
                    Initiates the OAuth2 authorization flow with Google.

                    **Flow:**
                    1. Client accesses this endpoint
                    2. Server redirects to Google's OAuth2 authorization page (HTTP 302)
                    3. User authenticates with Google
                    4. Google redirects back to the backend callback
                    5. Backend creates/links the user and issues a one-time login code
                    6. Browser is redirected to the frontend with `?code=...` only (no tokens)
                    7. Frontend exchanges the code via POST /api/v1/auth/oauth/exchange

                    **Note:** This endpoint performs an HTTP 302 redirect and cannot be tested
                    directly in Swagger UI. Use a browser to initiate the OAuth2 flow.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirect to Google OAuth2 authorization page"
            )
    })
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }
}
