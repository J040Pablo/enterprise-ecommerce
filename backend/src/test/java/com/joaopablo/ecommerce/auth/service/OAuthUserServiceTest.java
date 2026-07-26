package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.entity.AuthProvider;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthService authService;

    @InjectMocks
    private OAuthUserService oAuthUserService;

    @Test
    void loginWithGoogleShouldCreateNewCustomerWhenEmailDoesNotExist() {
        OAuth2User oauth2User = googleUser(
                "new.user@email.com",
                "google-sub-1",
                "Ana",
                "Silva",
                "https://example.com/ana.jpg"
        );

        Role customerRole = Role.builder().name("CUSTOMER").description("Customer").build();
        when(userRepository.findByEmail("new.user@email.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded-random-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        LoginResponseDTO expected = LoginResponseDTO.builder()
                .token("access")
                .refreshToken("refresh")
                .type("Bearer")
                .expiresIn(86400000)
                .build();
        when(authService.issueTokens(any(User.class))).thenReturn(expected);

        LoginResponseDTO response = oAuthUserService.loginWithGoogle(oauth2User);

        assertSame(expected, response);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User created = userCaptor.getValue();

        assertEquals("new.user@email.com", created.getEmail());
        assertEquals("google-sub-1", created.getGoogleId());
        assertEquals("Ana", created.getFirstName());
        assertEquals("Silva", created.getLastName());
        assertEquals("https://example.com/ana.jpg", created.getProfileImageUrl());
        assertEquals(AuthProvider.GOOGLE, created.getProvider());
        assertEquals("encoded-random-password", created.getPassword());
        assertTrue(created.getEmailVerified());
        assertEquals(1, created.getUserRoles().size());
        verify(authService).issueTokens(created);
    }

    @Test
    void loginWithGoogleShouldReuseExistingAccountAndUpdateProfile() {
        OAuth2User oauth2User = googleUser(
                "existing@email.com",
                "google-sub-2",
                "Joao",
                "Pablo",
                "https://example.com/joao.jpg"
        );

        User existing = User.builder()
                .email("existing@email.com")
                .password("local-password")
                .firstName("Old")
                .lastName("Name")
                .provider(AuthProvider.LOCAL)
                .userRoles(Set.of())
                .build();
        existing.setId(UUID.randomUUID());

        when(userRepository.findByEmail("existing@email.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponseDTO expected = LoginResponseDTO.builder()
                .token("access")
                .refreshToken("refresh")
                .type("Bearer")
                .build();
        when(authService.issueTokens(any(User.class))).thenReturn(expected);

        LoginResponseDTO response = oAuthUserService.loginWithGoogle(oauth2User);

        assertSame(expected, response);
        assertEquals("google-sub-2", existing.getGoogleId());
        assertEquals("Joao", existing.getFirstName());
        assertEquals("Pablo", existing.getLastName());
        assertEquals("https://example.com/joao.jpg", existing.getProfileImageUrl());
        assertEquals(AuthProvider.LOCAL, existing.getProvider());
        assertEquals("local-password", existing.getPassword());
        verify(roleRepository, never()).findByName(any());
        verify(authService).issueTokens(existing);
    }

    private OAuth2User googleUser(
            String email,
            String sub,
            String givenName,
            String familyName,
            String picture
    ) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", sub);
        attributes.put("email", email);
        attributes.put("given_name", givenName);
        attributes.put("family_name", familyName);
        attributes.put("picture", picture);
        attributes.put("name", givenName + " " + familyName);

        return new DefaultOAuth2User(
                List.of(() -> "ROLE_USER"),
                attributes,
                "sub"
        );
    }
}
