package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.entity.User;

import java.util.List;

public interface UserService {

    User findById(Long id);

    User findByEmail(String email);

    List<User> findAll();
}
