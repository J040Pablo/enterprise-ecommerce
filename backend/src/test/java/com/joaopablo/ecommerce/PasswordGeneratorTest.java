package com.joaopablo.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGeneratorTest {

    @Test
    void generatePassword() {
        System.out.println(new BCryptPasswordEncoder().encode("Admin@123"));
    }
}