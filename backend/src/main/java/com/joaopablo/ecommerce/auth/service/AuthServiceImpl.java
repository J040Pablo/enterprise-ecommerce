package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.dto.request.CreateUserRequest;
import com.joaopablo.ecommerce.auth.dto.response.UserResponse;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.mapper.UserMapper;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.common.exception.ResourceAlreadyExistsException;
import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

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

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(customerRole)
                .build();

        // maintain bidirectional relationship in memory
        user.getUserRoles().add(userRole);
        customerRole.getUserRoles().add(userRole);

        User saved = userRepository.save(user);

        return userMapper.toResponse(saved);
    }
}
