package com.joaopablo.ecommerce.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joaopablo.ecommerce.auth.dto.request.LoginRequestDTO;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.auth.repository.UserRoleRepository;
import com.joaopablo.ecommerce.category.entity.Category;
import com.joaopablo.ecommerce.category.repository.CategoryRepository;
import com.joaopablo.ecommerce.inventory.entity.Inventory;
import com.joaopablo.ecommerce.inventory.repository.InventoryRepository;
import com.joaopablo.ecommerce.order.dto.request.CreateOrderRequest;
import com.joaopablo.ecommerce.order.dto.request.OrderItemRequest;
import com.joaopablo.ecommerce.order.dto.response.OrderResponse;
import com.joaopablo.ecommerce.order.service.OrderService;
import com.joaopablo.ecommerce.product.entity.Product;
import com.joaopablo.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserOrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedUser;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("CUSTOMER").description("Customer role").build()));

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").description("Admin role").build()));

        User user = User.builder()
                .firstName("Integration")
                .lastName("TestUser")
                .email("user.order.test@email.com")
                .password(passwordEncoder.encode("Password@123"))
                .cpf("98765432100")
                .phone("11988887777")
                .enabled(true)
                .emailVerified(true)
                .build();

        UserRole userRoleCustomer = UserRole.builder()
                .user(user)
                .role(customerRole)
                .build();

        UserRole userRoleAdmin = UserRole.builder()
                .user(user)
                .role(adminRole)
                .build();

        user.getUserRoles().add(userRoleCustomer);
        user.getUserRoles().add(userRoleAdmin);

        savedUser = userRepository.save(user);

        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .description("Electronic gadgets")
                .build());

        savedProduct = productRepository.save(Product.builder()
                .name("Test Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("1500.00"))
                .active(true)
                .category(category)
                .build());

        inventoryRepository.save(Inventory.builder()
                .product(savedProduct)
                .quantity(10)
                .build());
    }

    @Test
    @DisplayName("Complete Integration Flow: Login -> Obtain User UUID -> Verify Roles (CUSTOMER, ADMIN) -> Create Order with User UUID")
    void completeAuthToOrderFlowWithUUID() throws Exception {
        // 1. Perform Login
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email("user.order.test@email.com")
                .password("Password@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponseDTO loginResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponseDTO.class);

        // 2. Validate Token and UUID return
        assertNotNull(loginResponse.getToken());
        assertNotNull(loginResponse.getUser());
        UUID returnedUserId = loginResponse.getUser().getId();
        assertNotNull(returnedUserId);
        assertEquals(savedUser.getId(), returnedUserId);

        // Validate Roles (CUSTOMER, ADMIN)
        List<String> roles = loginResponse.getUser().getRoles();
        assertTrue(roles.contains("CUSTOMER"));
        assertTrue(roles.contains("ADMIN"));

        // 3. Create Order using the User's UUID
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .userId(returnedUserId)
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId(savedProduct.getId())
                                .quantity(2)
                                .build()
                ))
                .build();

        OrderResponse orderResponse = orderService.createOrder(createOrderRequest);

        // 4. Validate Created Order
        assertNotNull(orderResponse);
        assertNotNull(orderResponse.getId());
        assertEquals(returnedUserId, orderResponse.getUserId());
        assertEquals(new BigDecimal("3000.00"), orderResponse.getTotalAmount());
        assertEquals(1, orderResponse.getItems().size());
        assertEquals("Test Laptop", orderResponse.getItems().get(0).getProductName());
    }
}
