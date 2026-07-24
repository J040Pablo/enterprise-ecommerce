package com.joaopablo.ecommerce.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.auth.repository.UserRoleRepository;
import com.joaopablo.ecommerce.cart.dto.request.CreateCartRequest;
import com.joaopablo.ecommerce.cart.dto.request.UpdateCartRequest;
import com.joaopablo.ecommerce.cart.entity.Cart;
import com.joaopablo.ecommerce.cart.repository.CartRepository;
import com.joaopablo.ecommerce.category.entity.Category;
import com.joaopablo.ecommerce.category.repository.CategoryRepository;
import com.joaopablo.ecommerce.inventory.entity.Inventory;
import com.joaopablo.ecommerce.inventory.repository.InventoryRepository;
import com.joaopablo.ecommerce.product.entity.Product;
import com.joaopablo.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    private CartRepository cartRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setup() {
        cartRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role customerRole = roleRepository.save(
                Role.builder().name("CUSTOMER").description("Customer role").build()
        );

        testUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("test.user@email.com")
                .password("password")
                .enabled(true)
                .emailVerified(true)
                .build();
        UserRole userRole = UserRole.builder().user(testUser).role(customerRole).build();
        testUser.getUserRoles().add(userRole);
        userRepository.save(testUser);

        Category category = categoryRepository.save(Category.builder().name("Electronics").build());

        testProduct = Product.builder()
                .name("Laptop")
                .price(new BigDecimal("1000.00"))
                .active(true)
                .category(category)
                .build();
        productRepository.save(testProduct);

        Inventory inventory = Inventory.builder().product(testProduct).quantity(10).build();
        inventoryRepository.save(inventory);
    }

    @Test
    @WithMockUser(username = "test.user@email.com", roles = "CUSTOMER")
    void getCart_ShouldReturnEmptyCart() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total").value(0.0));
    }

    @Test
    @WithMockUser(username = "test.user@email.com", roles = "CUSTOMER")
    void addItemToCart_ShouldReturnCartWithItem() throws Exception {
        CreateCartRequest request = CreateCartRequest.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(2000.0))
                .andExpect(jsonPath("$.total").value(2000.0));
    }

    @Test
    @WithMockUser(username = "test.user@email.com", roles = "CUSTOMER")
    void addItemToCart_Twice_ShouldMergeQuantity() throws Exception {
        CreateCartRequest request = CreateCartRequest.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(4))
                .andExpect(jsonPath("$.total").value(4000.0));
    }

    @Test
    @WithMockUser(username = "test.user@email.com", roles = "CUSTOMER")
    void updateItemQuantity_ShouldReturnUpdatedCart() throws Exception {
        CreateCartRequest addRequest = CreateCartRequest.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk());

        UpdateCartRequest updateRequest = UpdateCartRequest.builder()
                .quantity(5)
                .build();

        mockMvc.perform(patch("/api/v1/cart/items/" + testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.total").value(5000.0));
    }

    @Test
    @WithMockUser(username = "test.user@email.com", roles = "CUSTOMER")
    void removeItemFromCart_ShouldReturnEmptyCart() throws Exception {
        CreateCartRequest addRequest = CreateCartRequest.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/cart/items/" + testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total").value(0.0));
    }

    @Test
    @WithMockUser(username = "test.user@email.com", roles = "CUSTOMER")
    void clearCart_ShouldReturnNoContent() throws Exception {
        CreateCartRequest addRequest = CreateCartRequest.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/cart"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @WithMockUser(username = "test.user@email.com", roles = "CUSTOMER")
    void addItemToCart_ProductNotFound_ShouldReturn404() throws Exception {
        CreateCartRequest request = CreateCartRequest.builder()
                .productId(UUID.randomUUID())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test.user@email.com", roles = "CUSTOMER")
    void addItemToCart_InvalidQuantity_ShouldReturn400() throws Exception {
        CreateCartRequest request = CreateCartRequest.builder()
                .productId(testProduct.getId())
                .quantity(0)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test.user@email.com", roles = "CUSTOMER")
    void addItemToCart_OutOfStock_ShouldReturn409() throws Exception {
        CreateCartRequest request = CreateCartRequest.builder()
                .productId(testProduct.getId())
                .quantity(15) // Stock is 10
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getCart_UnauthorizedAccess_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized()); // Or another expected unauthorized status
    }
}
