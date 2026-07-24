package com.joaopablo.ecommerce.common.util;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
    Authentication getAuthentication();
    String getAuthenticatedUserEmail();
}
