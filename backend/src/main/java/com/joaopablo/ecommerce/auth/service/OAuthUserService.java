package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.entity.AuthProvider;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OAuthUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Transactional
    public LoginResponseDTO loginWithGoogle(OAuth2User oauth2User) {
        String email = requireAttribute(oauth2User, "email");
        String googleId = requireAttribute(oauth2User, "sub");
        String firstName = firstNonBlank(
                oauth2User.getAttribute("given_name"),
                extractFirstName(oauth2User.getAttribute("name"))
        );
        String lastName = firstNonBlank(
                oauth2User.getAttribute("family_name"),
                extractLastName(oauth2User.getAttribute("name"))
        );
        String picture = oauth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email)
                .map(existing -> updateGoogleProfile(existing, googleId, firstName, lastName, picture))
                .orElseGet(() -> createGoogleUser(email, googleId, firstName, lastName, picture));

        return authService.issueTokens(user);
    }

    private User updateGoogleProfile(
            User user,
            String googleId,
            String firstName,
            String lastName,
            String picture
    ) {
        if (!StringUtils.hasText(user.getGoogleId())) {
            user.setGoogleId(googleId);
        }
        if (StringUtils.hasText(firstName)) {
            user.setFirstName(firstName);
        }
        if (StringUtils.hasText(lastName)) {
            user.setLastName(lastName);
        }
        if (StringUtils.hasText(picture)) {
            user.setProfileImageUrl(picture);
        }
        if (user.getProvider() == null) {
            user.setProvider(AuthProvider.GOOGLE);
        }
        return userRepository.save(user);
    }

    private User createGoogleUser(
            String email,
            String googleId,
            String firstName,
            String lastName,
            String picture
    ) {
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER not found."));

        User user = User.builder()
                .email(email)
                .googleId(googleId)
                .firstName(firstName)
                .lastName(lastName)
                .profileImageUrl(picture)
                .provider(AuthProvider.GOOGLE)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .enabled(true)
                .emailVerified(true)
                .build();

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(customerRole)
                .build();

        user.getUserRoles().add(userRole);
        customerRole.getUserRoles().add(userRole);

        return userRepository.save(user);
    }

    private String requireAttribute(OAuth2User oauth2User, String key) {
        Object value = oauth2User.getAttribute(key);
        if (value == null || !StringUtils.hasText(value.toString())) {
            throw new IllegalArgumentException("Missing required OAuth2 attribute: " + key);
        }
        return value.toString();
    }

    private String firstNonBlank(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary;
        }
        return fallback;
    }

    private String extractFirstName(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return null;
        }
        String trimmed = fullName.trim();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed;
    }

    private String extractLastName(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return null;
        }
        String trimmed = fullName.trim();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(space + 1).trim() : null;
    }
}
