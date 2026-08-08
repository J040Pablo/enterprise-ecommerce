package com.joaopablo.ecommerce.common.security;

import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.common.util.AuthenticationFacade;
import com.joaopablo.ecommerce.order.entity.Order;
import com.joaopablo.ecommerce.order.exception.OrderNotFoundException;
import com.joaopablo.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Shared ownership checks for resources linked to an {@link Order}
 * (Payment, Shipping, etc.): ADMIN may access any; CUSTOMER only own orders.
 */
@Component
@RequiredArgsConstructor
public class ResourceOwnershipService {

    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public void assertOrderOwnerOrAdmin(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        assertOwnerOrAdmin(order.getUserId());
    }

    public void assertOwnerOrAdmin(UUID resourceOwnerId) {
        if (isAdmin()) {
            return;
        }
        UUID authenticatedUserId = requireAuthenticatedUserId();
        if (!authenticatedUserId.equals(resourceOwnerId)) {
            throw new AccessDeniedException("Access denied to this resource");
        }
    }

    public UUID requireAuthenticatedUserId() {
        String email = authenticationFacade.getAuthenticatedUserEmail();
        if (email == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    public boolean isAdmin() {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
