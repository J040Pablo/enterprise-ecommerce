package com.joaopablo.ecommerce.order.service;

import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.common.util.AuthenticationFacade;
import com.joaopablo.ecommerce.inventory.service.InventoryService;
import com.joaopablo.ecommerce.order.dto.request.CreateOrderRequest;
import com.joaopablo.ecommerce.order.dto.request.OrderItemRequest;
import com.joaopablo.ecommerce.order.dto.request.UpdateOrderStatusRequest;
import com.joaopablo.ecommerce.order.dto.response.OrderResponse;
import com.joaopablo.ecommerce.order.dto.response.PaymentSummaryResponse;
import com.joaopablo.ecommerce.order.dto.response.ShippingSummaryResponse;
import com.joaopablo.ecommerce.order.entity.Order;
import com.joaopablo.ecommerce.order.entity.OrderItem;
import com.joaopablo.ecommerce.order.entity.OrderStatus;
import com.joaopablo.ecommerce.order.exception.OrderNotFoundException;
import com.joaopablo.ecommerce.order.mapper.OrderMapper;
import com.joaopablo.ecommerce.order.messaging.OrderEventPublisher;
import com.joaopablo.ecommerce.order.repository.OrderRepository;
import com.joaopablo.ecommerce.payment.service.PaymentService;
import com.joaopablo.ecommerce.product.dto.response.ProductResponse;
import com.joaopablo.ecommerce.product.service.ProductService;
import com.joaopablo.ecommerce.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final OrderEventPublisher orderEventPublisher;
    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        UUID orderUserId = resolveOrderUserId(request.getUserId());

        Order order = new Order();
        order.setUserId(orderUserId);
        order.setStatus(OrderStatus.PENDING);

        for (OrderItemRequest itemRequest : request.getItems()) {

            ProductResponse product = productService.findById(itemRequest.getProductId());

            inventoryService.decreaseStock(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.id());
            orderItem.setProductName(product.name());
            orderItem.setUnitPrice(product.price());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.calculateSubtotal();

            order.addItem(orderItem);
        }

        order.calculateTotal();

        Order savedOrder = repository.save(order);
        orderEventPublisher.publishOrderCreated(savedOrder);

        paymentService.createPayment(savedOrder);

        return toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        Order order = findEntityById(id);
        assertOwnerOrAdmin(order.getUserId());
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByUser(UUID userId) {
        assertOwnerOrAdmin(userId);
        return repository.findByUserId(userId).stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = findEntityById(id);
        order.changeStatus(request.getStatus());

        return toOrderResponse(repository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(UUID id) {

        Order order = findEntityById(id);
        assertOwnerOrAdmin(order.getUserId());

        order.changeStatus(OrderStatus.CANCELLED);

        order.getItems().forEach(item -> {

            inventoryService.increaseStock(
                    item.getProductId(),
                    item.getQuantity());

        });

        Order savedOrder = repository.save(order);
        orderEventPublisher.publishOrderCancelled(savedOrder);

        return toOrderResponse(savedOrder);
    }

    private OrderResponse toOrderResponse(Order order) {
        return mapper.toResponse(
                order,
                toPaymentSummary(order.getId()),
                toShippingSummary(order.getId())
        );
    }

    private PaymentSummaryResponse toPaymentSummary(UUID orderId) {
        return paymentService.findByOrderId(orderId)
                .map(payment -> PaymentSummaryResponse.builder()
                        .id(payment.getId())
                        .status(payment.getStatus())
                        .build())
                .orElse(null);
    }

    private ShippingSummaryResponse toShippingSummary(UUID orderId) {
        return shippingService.findByOrderId(orderId)
                .map(shipping -> ShippingSummaryResponse.builder()
                        .id(shipping.getId())
                        .trackingCode(shipping.getTrackingCode())
                        .status(shipping.getStatus())
                        .build())
                .orElse(null);
    }

    private Order findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    /**
     * CUSTOMER orders are always bound to the authenticated user.
     * ADMIN may place an order on behalf of the {@code requestUserId}.
     */
    private UUID resolveOrderUserId(UUID requestUserId) {
        UUID authenticatedUserId = requireAuthenticatedUserId();
        if (isAdmin()) {
            return requestUserId != null ? requestUserId : authenticatedUserId;
        }
        if (requestUserId != null && !requestUserId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Cannot create an order for another user");
        }
        return authenticatedUserId;
    }

    private void assertOwnerOrAdmin(UUID resourceOwnerId) {
        if (isAdmin()) {
            return;
        }
        UUID authenticatedUserId = requireAuthenticatedUserId();
        if (!authenticatedUserId.equals(resourceOwnerId)) {
            throw new AccessDeniedException("Access denied to this order resource");
        }
    }

    private UUID requireAuthenticatedUserId() {
        String email = authenticationFacade.getAuthenticatedUserEmail();
        if (email == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private boolean isAdmin() {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
