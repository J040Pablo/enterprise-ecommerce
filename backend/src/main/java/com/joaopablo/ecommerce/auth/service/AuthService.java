package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.dto.request.CreateUserRequest;
import com.joaopablo.ecommerce.auth.dto.request.LoginRequestDTO;
import com.joaopablo.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.TokenRefreshResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.UserResponse;
import com.joaopablo.ecommerce.auth.entity.User;

import com.joaopablo.ecommerce.auth.dto.request.LogoutRequestDTO;

public interface AuthService {

    UserResponse register(CreateUserRequest request);

    LoginResponseDTO login(LoginRequestDTO request);

    LoginResponseDTO issueTokens(User user);

    TokenRefreshResponseDTO refresh(RefreshTokenRequestDTO request);

    void logout(LogoutRequestDTO request);
}
