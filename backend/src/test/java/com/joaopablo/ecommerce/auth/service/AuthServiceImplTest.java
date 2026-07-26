package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.TokenRefreshResponseDTO;
import com.joaopablo.ecommerce.auth.entity.RefreshToken;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.mapper.UserMapper;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;


    @InjectMocks
    private AuthServiceImpl authService;



    @Test
    void issueTokensShouldReturnAccessAndRefreshTokens() {

        Role role =
                Role.builder()
                        .name("CUSTOMER")
                        .build();


        User user =
                User.builder()
                        .email("user@email.com")
                        .firstName("Joao")
                        .lastName("Pablo")
                        .password("encoded")
                        .userRoles(new HashSet<>())
                        .build();


        user.setId(UUID.randomUUID());


        user.getUserRoles()
                .add(
                        UserRole.builder()
                                .user(user)
                                .role(role)
                                .build()
                );


        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token("refresh-123")
                        .user(user)
                        .build();



        when(jwtService.generateToken("user@email.com"))
                .thenReturn("access-123");

        when(jwtService.getExpirationMs())
                .thenReturn(86400000L);

        when(refreshTokenService.create(user))
                .thenReturn(refreshToken);



        LoginResponseDTO response =
                authService.issueTokens(user);



        assertEquals(
                "access-123",
                response.getToken()
        );

        assertEquals(
                "refresh-123",
                response.getRefreshToken()
        );

        assertEquals(
                "Bearer",
                response.getType()
        );

        assertEquals(
                86400000L,
                response.getExpiresIn()
        );

        assertEquals(
                "user@email.com",
                response.getUser().getEmail()
        );

        assertEquals(
                Set.of("CUSTOMER"),
                Set.copyOf(response.getUser().getRoles())
        );
    }



    @Test
    void refreshShouldRotateRefreshTokenAndIssueNewAccessToken() {

        User user =
                User.builder()
                        .email("user@email.com")
                        .password("encoded")
                        .build();


        user.setId(UUID.randomUUID());


        RefreshToken rotated =
                RefreshToken.builder()
                        .token("new-refresh")
                        .user(user)
                        .build();



        when(refreshTokenService.rotate("old-refresh"))
                .thenReturn(rotated);

        when(jwtService.generateToken("user@email.com"))
                .thenReturn("new-access");

        when(jwtService.getExpirationMs())
                .thenReturn(86400000L);



        TokenRefreshResponseDTO response =
                authService.refresh(
                        RefreshTokenRequestDTO.builder()
                                .refreshToken("old-refresh")
                                .build()
                );



        assertEquals(
                "new-access",
                response.getAccessToken()
        );

        assertEquals(
                "new-refresh",
                response.getRefreshToken()
        );

        assertEquals(
                "Bearer",
                response.getType()
        );

        assertEquals(
                86400000L,
                response.getExpiresIn()
        );


        verify(refreshTokenService)
                .rotate("old-refresh");
    }
}