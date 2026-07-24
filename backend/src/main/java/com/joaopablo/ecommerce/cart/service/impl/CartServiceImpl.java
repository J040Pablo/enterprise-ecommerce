package com.joaopablo.ecommerce.cart.service.impl;

import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.cart.dto.request.CreateCartRequest;
import com.joaopablo.ecommerce.cart.dto.request.UpdateCartRequest;
import com.joaopablo.ecommerce.cart.dto.response.CartItemResponse;
import com.joaopablo.ecommerce.cart.dto.response.CartResponse;
import com.joaopablo.ecommerce.cart.entity.Cart;
import com.joaopablo.ecommerce.cart.entity.CartItem;
import com.joaopablo.ecommerce.cart.exception.CartItemNotFoundException;
import com.joaopablo.ecommerce.cart.mapper.CartMapper;
import com.joaopablo.ecommerce.cart.repository.CartRepository;
import com.joaopablo.ecommerce.cart.service.CartService;
import com.joaopablo.ecommerce.common.exception.BusinessException;
import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;
import com.joaopablo.ecommerce.common.util.AuthenticationFacade;
import com.joaopablo.ecommerce.inventory.entity.Inventory;
import com.joaopablo.ecommerce.inventory.exception.InsufficientStockException;
import com.joaopablo.ecommerce.inventory.repository.InventoryRepository;
import com.joaopablo.ecommerce.product.entity.Product;
import com.joaopablo.ecommerce.product.exception.ProductNotFoundException;
import com.joaopablo.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CartMapper cartMapper;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public CartResponse getCart() {
        String email = getAuthenticatedUserEmail();
        Cart cart = getOrCreateCart(email);
        return convertToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(CreateCartRequest request) {
        String email = getAuthenticatedUserEmail();
        Cart cart = getOrCreateCart(email);

        UUID productId = request.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getActive() == null || !product.getActive()) {
            throw new BusinessException("Product with ID " + productId + " is inactive.");
        }

        // Find if item is already in cart
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        int currentQuantityInCart = existingItemOpt.map(CartItem::getQuantity).orElse(0);
        int targetQuantity = currentQuantityInCart + request.getQuantity();

        // Validate stock
        validateStock(productId, targetQuantity);

        if (existingItemOpt.isPresent()) {
            existingItemOpt.get().setQuantity(targetQuantity);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(cartItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return convertToResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(UUID productId, UpdateCartRequest request) {
        String email = getAuthenticatedUserEmail();
        Cart cart = getOrCreateCart(email);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        Product product = cartItem.getProduct();
        if (product.getActive() == null || !product.getActive()) {
            throw new BusinessException("Product with ID " + productId + " is inactive.");
        }

        int targetQuantity = request.getQuantity();

        // Validate stock
        validateStock(productId, targetQuantity);

        cartItem.setQuantity(targetQuantity);

        Cart savedCart = cartRepository.save(cart);
        return convertToResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(UUID productId) {
        String email = getAuthenticatedUserEmail();
        Cart cart = getOrCreateCart(email);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        cart.removeItem(cartItem);

        Cart savedCart = cartRepository.save(cart);
        return convertToResponse(savedCart);
    }

    @Override
    @Transactional
    public void clearCart() {
        String email = getAuthenticatedUserEmail();
        Cart cart = getOrCreateCart(email);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private String getAuthenticatedUserEmail() {
        String email = authenticationFacade.getAuthenticatedUserEmail();
        if (email == null) {
            throw new BusinessException("No authenticated user session found.");
        }
        return email;
    }

    private Cart getOrCreateCart(String email) {
        return cartRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private void validateStock(UUID productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InsufficientStockException("Inventory not found for product: " + productId));

        if (inventory.getQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock available for product ID " + productId
                    + ". Requested: " + quantity + ", Available: " + inventory.getQuantity());
        }
    }

    private CartResponse convertToResponse(Cart cart) {
        return cartMapper.toResponse(cart);
    }
}
