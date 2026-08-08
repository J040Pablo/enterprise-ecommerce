package com.joaopablo.ecommerce.common.security;

import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.common.util.AuthenticationFacade;
import com.joaopablo.ecommerce.order.entity.Order;
import com.joaopablo.ecommerce.order.exception.OrderNotFoundException;
import com.joaopablo.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceOwnershipServiceTest {

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ResourceOwnershipService service;

    private UUID ownerId;
    private UUID orderId;
    private User owner;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        owner = new User();
        owner.setId(ownerId);
        owner.setEmail("owner@email.com");
    }

    @Test
    void customerMayAccessOwnOrder() {
        mockCustomer();
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(ownerId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> service.assertOrderOwnerOrAdmin(orderId));
    }

    @Test
    void customerMayNotAccessAnotherUsersOrder() {
        mockCustomer();
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(UUID.randomUUID());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> service.assertOrderOwnerOrAdmin(orderId));
    }

    @Test
    void adminMayAccessAnyOrder() {
        when(authenticationFacade.getAuthentication()).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        "admin@email.com",
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(UUID.randomUUID());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> service.assertOrderOwnerOrAdmin(orderId));
    }

    @Test
    void missingOrderThrowsNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.assertOrderOwnerOrAdmin(orderId));
    }

    private void mockCustomer() {
        when(authenticationFacade.getAuthentication()).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        owner.getEmail(),
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                )
        );
        when(authenticationFacade.getAuthenticatedUserEmail()).thenReturn(owner.getEmail());
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
    }
}
