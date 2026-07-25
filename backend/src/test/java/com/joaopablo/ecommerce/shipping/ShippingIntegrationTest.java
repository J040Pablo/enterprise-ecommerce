package com.joaopablo.ecommerce.shipping;

import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.category.entity.Category;
import com.joaopablo.ecommerce.category.repository.CategoryRepository;
import com.joaopablo.ecommerce.inventory.entity.Inventory;
import com.joaopablo.ecommerce.inventory.repository.InventoryRepository;
import com.joaopablo.ecommerce.order.dto.request.CreateOrderRequest;
import com.joaopablo.ecommerce.order.dto.request.OrderItemRequest;
import com.joaopablo.ecommerce.order.dto.response.OrderResponse;
import com.joaopablo.ecommerce.order.entity.OrderStatus;
import com.joaopablo.ecommerce.order.repository.OrderRepository;
import com.joaopablo.ecommerce.order.service.OrderService;
import com.joaopablo.ecommerce.payment.repository.PaymentRepository;
import com.joaopablo.ecommerce.payment.service.PaymentService;
import com.joaopablo.ecommerce.product.entity.Product;
import com.joaopablo.ecommerce.product.repository.ProductRepository;
import com.joaopablo.ecommerce.shipping.dto.request.CreateShippingRequest;
import com.joaopablo.ecommerce.shipping.dto.response.ShippingResponse;
import com.joaopablo.ecommerce.shipping.entity.ShippingStatus;
import com.joaopablo.ecommerce.shipping.exception.InvalidShippingStatusException;
import com.joaopablo.ecommerce.shipping.exception.ShippingAlreadyExistsException;
import com.joaopablo.ecommerce.shipping.exception.ShippingCreationException;
import com.joaopablo.ecommerce.shipping.repository.ShippingRepository;
import com.joaopablo.ecommerce.shipping.service.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ShippingIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ShippingService shippingService;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedUser;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("CUSTOMER").description("Customer role").build()));

        User user = User.builder()
                .firstName("Shipping")
                .lastName("Tester")
                .email("shipping.integration@email.com")
                .password(passwordEncoder.encode("Password@123"))
                .cpf("55566677788")
                .phone("11988887777")
                .enabled(true)
                .emailVerified(true)
                .build();

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(customerRole)
                .build();
        user.getUserRoles().add(userRole);
        savedUser = userRepository.save(user);

        Category category = categoryRepository.save(Category.builder()
                .name("Shipping Category")
                .description("Category for shipping tests")
                .build());

        savedProduct = productRepository.save(Product.builder()
                .name("Shipping Product")
                .description("Product for shipping flow")
                .price(new BigDecimal("80.00"))
                .active(true)
                .category(category)
                .build());

        inventoryRepository.save(Inventory.builder()
                .product(savedProduct)
                .quantity(20)
                .build());
    }

    @Test
    @DisplayName("Approve payment creates PROCESSING shipping automatically")
    void shouldCreateShippingWhenPaymentIsApproved() {
        OrderResponse order = createOrder(1);
        var payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();

        paymentService.approvePayment(payment.getId());

        var shipping = shippingRepository.findByOrderId(order.getId()).orElseThrow();
        assertEquals(ShippingStatus.PROCESSING, shipping.getStatus());
        assertNotNull(shipping.getTrackingCode());
        assertTrue(shipping.getTrackingCode().startsWith("BR"));
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Find shipping by id and by order id")
    void shouldFindShippingByIdAndOrderId() {
        OrderResponse order = createConfirmedOrder(1);
        var shipping = shippingRepository.findByOrderId(order.getId()).orElseThrow();

        ShippingResponse byId = shippingService.findById(shipping.getId());
        ShippingResponse byOrder = shippingService.findByOrderId(order.getId()).orElseThrow();

        assertEquals(shipping.getId(), byId.getId());
        assertEquals(order.getId(), byOrder.getOrderId());
        assertEquals(ShippingStatus.PROCESSING, byId.getStatus());
    }

    @Test
    @DisplayName("Cannot create two shippings for the same order")
    void shouldNotAllowDuplicateShipping() {
        OrderResponse order = createConfirmedOrder(1);

        CreateShippingRequest request = CreateShippingRequest.builder()
                .orderId(order.getId())
                .carrier("CORREIOS")
                .estimatedDelivery(LocalDate.now().plusDays(3))
                .build();

        assertThrows(ShippingAlreadyExistsException.class, () -> shippingService.createShipping(request));
    }

    @Test
    @DisplayName("Full forwarding status flow")
    void shouldAdvanceThroughFullShippingFlow() {
        OrderResponse order = createConfirmedOrder(1);
        UUID shippingId = shippingRepository.findByOrderId(order.getId()).orElseThrow().getId();

        assertEquals(ShippingStatus.SHIPPED, shippingService.markAsShipped(shippingId).getStatus());
        assertEquals(ShippingStatus.OUT_FOR_DELIVERY, shippingService.markAsOutForDelivery(shippingId).getStatus());
        assertEquals(ShippingStatus.DELIVERED, shippingService.markAsDelivered(shippingId).getStatus());

        var shipping = shippingRepository.findById(shippingId).orElseThrow();
        assertNotNull(shipping.getShippedAt());
        assertNotNull(shipping.getDeliveredAt());
    }

    @Test
    @DisplayName("Cancel PROCESSING shipping")
    void shouldCancelProcessingShipping() {
        OrderResponse order = createConfirmedOrder(1);
        UUID shippingId = shippingRepository.findByOrderId(order.getId()).orElseThrow().getId();

        ShippingResponse cancelled = shippingService.cancelShipping(shippingId);

        assertEquals(ShippingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("Cannot cancel already shipped shipping")
    void shouldNotCancelShippedShipping() {
        OrderResponse order = createConfirmedOrder(1);
        UUID shippingId = shippingRepository.findByOrderId(order.getId()).orElseThrow().getId();
        shippingService.markAsShipped(shippingId);

        assertThrows(InvalidShippingStatusException.class, () -> shippingService.cancelShipping(shippingId));
    }

    @Test
    @DisplayName("Refund cancels PROCESSING shipping")
    void shouldCancelShippingOnRefund() {
        OrderResponse order = createConfirmedOrder(1);
        var payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();

        paymentService.refundPayment(payment.getId());

        var shipping = shippingRepository.findByOrderId(order.getId()).orElseThrow();
        assertEquals(ShippingStatus.CANCELLED, shipping.getStatus());
        assertEquals(OrderStatus.CANCELLED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Cannot create shipping for cancelled order")
    void shouldNotCreateShippingForCancelledOrder() {
        OrderResponse order = createOrder(1);
        var payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();
        paymentService.rejectPayment(payment.getId());

        CreateShippingRequest request = CreateShippingRequest.builder()
                .orderId(order.getId())
                .carrier("CORREIOS")
                .estimatedDelivery(LocalDate.now().plusDays(3))
                .build();

        assertThrows(ShippingCreationException.class, () -> shippingService.createShipping(request));
    }

    @Test
    @DisplayName("Cannot create shipping for non-confirmed order")
    void shouldNotCreateShippingForPendingOrder() {
        OrderResponse order = createOrder(1);

        CreateShippingRequest request = CreateShippingRequest.builder()
                .orderId(order.getId())
                .carrier("CORREIOS")
                .estimatedDelivery(LocalDate.now().plusDays(3))
                .build();

        assertThrows(ShippingCreationException.class, () -> shippingService.createShipping(request));
    }

    @Test
    @DisplayName("Cannot create shipping for nonexistent order")
    void shouldNotCreateShippingForNonexistentOrder() {
        CreateShippingRequest request = CreateShippingRequest.builder()
                .orderId(UUID.randomUUID())
                .carrier("CORREIOS")
                .estimatedDelivery(LocalDate.now().plusDays(3))
                .build();

        assertThrows(Exception.class, () -> shippingService.createShipping(request));
    }

    private OrderResponse createOrder(int quantity) {
        return orderService.createOrder(CreateOrderRequest.builder()
                .userId(savedUser.getId())
                .items(List.of(OrderItemRequest.builder()
                        .productId(savedProduct.getId())
                        .quantity(quantity)
                        .build()))
                .build());
    }

    private OrderResponse createConfirmedOrder(int quantity) {
        OrderResponse order = createOrder(quantity);
        var payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();
        paymentService.approvePayment(payment.getId());
        return order;
    }
}
