package com.joaopablo.ecommerce.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidOAuthLoginCodeException extends AuthenticationException {

    public InvalidOAuthLoginCodeException(String message) {
        super(message);
    }
}
