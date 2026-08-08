package com.joaopablo.ecommerce.payment;

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
import com.joaopablo.ecommerce.payment.dto.response.PaymentResponse;
import com.joaopablo.ecommerce.payment.entity.PaymentStatus;
import com.joaopablo.ecommerce.payment.repository.PaymentRepository;
import com.joaopablo.ecommerce.payment.service.PaymentService;
import com.joaopablo.ecommerce.product.entity.Product;
import com.joaopablo.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

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
                .firstName("Payment")
                .lastName("Tester")
                .email("payment.integration@email.com")
                .password(passwordEncoder.encode("Password@123"))
                .cpf("11122233344")
                .phone("11999998888")
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
                .name("Payment Category")
                .description("Category for payment tests")
                .build());

        savedProduct = productRepository.save(Product.builder()
                .name("Payment Product")
                .description("Product for payment flow")
                .price(new BigDecimal("100.00"))
                .active(true)
                .category(category)
                .build());

        inventoryRepository.save(Inventory.builder()
                .product(savedProduct)
                .quantity(10)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        savedUser.getEmail(),
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                )
        );
    }

    @Test
    @DisplayName("Create order creates PENDING payment; approve confirms order")
    void shouldCreatePendingPaymentAndApproveFlow() {
        OrderResponse order = orderService.createOrder(CreateOrderRequest.builder()
                .userId(savedUser.getId())
                .items(List.of(OrderItemRequest.builder()
                        .productId(savedProduct.getId())
                        .quantity(2)
                        .build()))
                .build());

        var payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(new BigDecimal("200.00"), payment.getAmount());

        PaymentResponse approved = paymentService.approvePayment(payment.getId());

        assertEquals(PaymentStatus.APPROVED, approved.getStatus());
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Reject payment cancels order and restores inventory")
    void shouldRejectPaymentAndRestoreInventory() {
        OrderResponse order = orderService.createOrder(CreateOrderRequest.builder()
                .userId(savedUser.getId())
                .items(List.of(OrderItemRequest.builder()
                        .productId(savedProduct.getId())
                        .quantity(3)
                        .build()))
                .build());

        assertEquals(7, inventoryRepository.findByProductId(savedProduct.getId()).orElseThrow().getQuantity());

        var payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();
        paymentService.rejectPayment(payment.getId());

        assertEquals(PaymentStatus.REJECTED, paymentRepository.findById(payment.getId()).orElseThrow().getStatus());
        assertEquals(OrderStatus.CANCELLED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
        assertEquals(10, inventoryRepository.findByProductId(savedProduct.getId()).orElseThrow().getQuantity());
    }

    @Test
    @DisplayName("Refund approved payment cancels order and restores inventory")
    void shouldRefundApprovedPaymentAndRestoreInventory() {
        OrderResponse order = orderService.createOrder(CreateOrderRequest.builder()
                .userId(savedUser.getId())
                .items(List.of(OrderItemRequest.builder()
                        .productId(savedProduct.getId())
                        .quantity(1)
                        .build()))
                .build());

        var payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();
        paymentService.approvePayment(payment.getId());
        paymentService.refundPayment(payment.getId());

        assertEquals(PaymentStatus.REFUNDED, paymentRepository.findById(payment.getId()).orElseThrow().getStatus());
        assertEquals(OrderStatus.CANCELLED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
        assertEquals(10, inventoryRepository.findByProductId(savedProduct.getId()).orElseThrow().getQuantity());
    }
}
