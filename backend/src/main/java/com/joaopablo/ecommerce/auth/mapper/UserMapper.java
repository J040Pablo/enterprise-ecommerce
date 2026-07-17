package com.joaopablo.ecommerce.auth.mapper;

import com.joaopablo.ecommerce.auth.dto.request.CreateUserRequest;
import com.joaopablo.ecommerce.auth.dto.response.UserResponse;
import com.joaopablo.ecommerce.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request, String encodedPassword) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(encodedPassword)
                .cpf(request.getCpf())
                .phone(request.getPhone())
                .enabled(true)
                .emailVerified(false)
                .build();
    }

    public UserResponse toResponse(User user) {
        
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .cpf(user.getCpf())
                .phone(user.getPhone())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}
