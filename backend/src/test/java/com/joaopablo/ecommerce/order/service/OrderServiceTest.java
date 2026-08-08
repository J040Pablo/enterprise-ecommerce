package com.joaopablo.ecommerce.order.service;

import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.common.util.AuthenticationFacade;
import com.joaopablo.ecommerce.inventory.exception.InsufficientStockException;
import com.joaopablo.ecommerce.inventory.service.InventoryService;
import com.joaopablo.ecommerce.order.dto.request.CreateOrderRequest;
import com.joaopablo.ecommerce.order.dto.request.OrderItemRequest;
import com.joaopablo.ecommerce.order.dto.request.UpdateOrderStatusRequest;
import com.joaopablo.ecommerce.order.dto.response.OrderResponse;
import com.joaopablo.ecommerce.order.entity.Order;
import com.joaopablo.ecommerce.order.entity.OrderStatus;
import com.joaopablo.ecommerce.order.exception.InvalidOrderStatusException;
import com.joaopablo.ecommerce.order.mapper.OrderMapper;
import com.joaopablo.ecommerce.order.messaging.OrderEventPublisher;
import com.joaopablo.ecommerce.order.repository.OrderRepository;
import com.joaopablo.ecommerce.payment.dto.response.PaymentResponse;
import com.joaopablo.ecommerce.payment.entity.PaymentStatus;
import com.joaopablo.ecommerce.payment.service.PaymentService;
import com.joaopablo.ecommerce.product.dto.response.ProductResponse;
import com.joaopablo.ecommerce.product.service.ProductService;
import com.joaopablo.ecommerce.shipping.service.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderMapper mapper;

    @Mock
    private ProductService productService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ShippingService shippingService;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService service;

    private UUID userId;
    private UUID productId;
    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        authenticatedUser = new User();
        authenticatedUser.setId(userId);
        authenticatedUser.setEmail("customer@email.com");
    }

    private void mockAuthenticatedCustomer() {
        when(authenticationFacade.getAuthenticatedUserEmail()).thenReturn(authenticatedUser.getEmail());
        when(userRepository.findByEmail(authenticatedUser.getEmail())).thenReturn(Optional.of(authenticatedUser));
        when(authenticationFacade.getAuthentication()).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser.getEmail(),
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                )
        );
    }

    private void mockAuthenticatedAdmin() {
        when(authenticationFacade.getAuthentication()).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser.getEmail(),
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        mockAuthenticatedCustomer();

        CreateOrderRequest request = new CreateOrderRequest(userId, List.of(
            new OrderItemRequest(productId, 2)
        ));

        ProductResponse product = ProductResponse.builder()
            .id(productId)
            .name("Product A")
            .price(BigDecimal.valueOf(100.0))
            .build();

        when(productService.findById(productId)).thenReturn(product);

        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setUserId(userId);
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setTotalAmount(BigDecimal.valueOf(200.0));

        OrderResponse mappedResponse = new OrderResponse();
        mappedResponse.setId(savedOrder.getId());
        mappedResponse.setTotalAmount(BigDecimal.valueOf(200.0));

        UUID paymentId = UUID.randomUUID();

        when(repository.save(any(Order.class))).thenReturn(savedOrder);
        when(paymentService.createPayment(savedOrder)).thenReturn(
                PaymentResponse.builder()
                        .id(paymentId)
                        .orderId(savedOrder.getId())
                        .amount(BigDecimal.valueOf(200.0))
                        .status(PaymentStatus.PENDING)
                        .build()
        );
        when(paymentService.findByOrderId(savedOrder.getId())).thenReturn(
                Optional.of(PaymentResponse.builder()
                        .id(paymentId)
                        .orderId(savedOrder.getId())
                        .status(PaymentStatus.PENDING)
                        .build())
        );
        when(shippingService.findByOrderId(savedOrder.getId())).thenReturn(Optional.empty());
        when(mapper.toResponse(eq(savedOrder), any(), any())).thenReturn(mappedResponse);

        OrderResponse response = service.createOrder(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(200.0), response.getTotalAmount());

        verify(inventoryService).decreaseStock(productId, 2);
        verify(repository).save(any(Order.class));
        verify(orderEventPublisher).publishOrderCreated(savedOrder);
        verify(paymentService).createPayment(savedOrder);
    }

    @Test
    void shouldRejectCreateOrderWhenImpersonatingAnotherUser() {
        mockAuthenticatedCustomer();
        UUID otherUserId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(otherUserId, List.of(
                new OrderItemRequest(productId, 1)
        ));

        assertThrows(AccessDeniedException.class, () -> service.createOrder(request));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldDenyAccessToAnotherUsersOrder() {
        mockAuthenticatedCustomer();
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> service.findById(orderId));
    }

    @Test
    void shouldAllowAdminToViewAnyOrder() {
        mockAuthenticatedAdmin();
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);

        OrderResponse mappedResponse = new OrderResponse();
        mappedResponse.setId(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentService.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(shippingService.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(mapper.toResponse(eq(order), any(), any())).thenReturn(mappedResponse);

        OrderResponse response = service.findById(orderId);

        assertEquals(orderId, response.getId());
    }

    @Test
    void shouldThrowInsufficientStockException() {
        mockAuthenticatedCustomer();

        CreateOrderRequest request = new CreateOrderRequest(userId, List.of(
            new OrderItemRequest(productId, 10)
        ));

        ProductResponse product = ProductResponse.builder()
            .id(productId)
            .name("Product A")
            .price(BigDecimal.valueOf(100.0))
            .build();

        when(productService.findById(productId)).thenReturn(product);
        doThrow(new InsufficientStockException("")).when(inventoryService).decreaseStock(productId, 10);

        assertThrows(InsufficientStockException.class, () -> service.createOrder(request));

        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldUpdateStatusSuccessfully() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.CONFIRMED);

        when(repository.save(order)).thenReturn(updatedOrder);

        OrderResponse mappedResponse = new OrderResponse();
        mappedResponse.setStatus(OrderStatus.CONFIRMED);

        when(paymentService.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(shippingService.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(mapper.toResponse(eq(updatedOrder), any(), any())).thenReturn(mappedResponse);

        OrderResponse response = service.updateStatus(orderId, new UpdateOrderStatusRequest(OrderStatus.CONFIRMED));

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenCancellingDeliveredOrder() {
        mockAuthenticatedCustomer();
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.DELIVERED);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class, () -> service.cancelOrder(orderId));
    }
}
