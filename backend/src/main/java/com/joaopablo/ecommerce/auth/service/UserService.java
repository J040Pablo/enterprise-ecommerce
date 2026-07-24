package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User findById(UUID id);

    User findByEmail(String email);

    List<User> findAll();
}
