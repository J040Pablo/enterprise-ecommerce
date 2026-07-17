package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.dto.request.CreateUserRequest;
import com.joaopablo.ecommerce.auth.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(CreateUserRequest request);

}
