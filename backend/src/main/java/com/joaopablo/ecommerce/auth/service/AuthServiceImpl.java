package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.dto.request.CreateUserRequest;
import com.joaopablo.ecommerce.auth.dto.request.LoginRequestDTO;
import com.joaopablo.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.TokenRefreshResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.UserResponse;
import com.joaopablo.ecommerce.auth.entity.AuthProvider;
import com.joaopablo.ecommerce.auth.entity.RefreshToken;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.mapper.UserMapper;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.auth.security.JwtService;
import com.joaopablo.ecommerce.common.exception.ResourceAlreadyExistsException;
import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public UserResponse register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists.");
        }

        if (userRepository.existsByCpf(request.getCpf())) {
            throw new ResourceAlreadyExistsException("CPF already exists.");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role CUSTOMER not found."));

        String encoded = passwordEncoder.encode(request.getPassword());

        User user = userMapper.toEntity(request, encoded);
        user.setProvider(AuthProvider.LOCAL);

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(customerRole)
                .build();

        user.getUserRoles().add(userRole);
        customerRole.getUserRoles().add(userRole);

        User saved = userRepository.save(user);

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        return issueTokens(user);
    }

    @Override
    @Transactional
    public LoginResponseDTO issueTokens(User user) {
        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.create(user);

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();

        LoginResponseDTO.UserLoginResponse userResponse = LoginResponseDTO.UserLoginResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(roles)
                .build();

        return LoginResponseDTO.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public TokenRefreshResponseDTO refresh(RefreshTokenRequestDTO request) {
        RefreshToken rotated = refreshTokenService.rotate(request.getRefreshToken());
        User user = rotated.getUser();

        String accessToken = jwtService.generateToken(user.getEmail());

        return TokenRefreshResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(rotated.getToken())
                .type("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }

    @Override
    @Transactional
    public void logout(com.joaopablo.ecommerce.auth.dto.request.LogoutRequestDTO request) {
        refreshTokenService.logout(request.getRefreshToken());
    }
}
