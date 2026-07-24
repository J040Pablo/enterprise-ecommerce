package com.joaopablo.ecommerce.cart.controller;

import com.joaopablo.ecommerce.cart.dto.request.CreateCartRequest;
import com.joaopablo.ecommerce.cart.dto.request.UpdateCartRequest;
import com.joaopablo.ecommerce.cart.dto.response.CartResponse;
import com.joaopablo.ecommerce.cart.service.CartService;
import com.joaopablo.ecommerce.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping Cart management APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Get current user's shopping cart", description = "Retrieves the shopping cart of the currently logged-in user. If the user doesn't have a cart, a new empty one is created.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shopping cart retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Add an item to the shopping cart", description = "Adds a product to the user's shopping cart. Validates product availability and active status. If product is already in cart, updates quantity by adding the new amount.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added to cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or business rule validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient stock available",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody CreateCartRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(request));
    }

    @PatchMapping("/items/{productId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Update the quantity of a product in the cart", description = "Updates the quantity of a product in the cart. Overwrites the item's previous quantity. Checks stock availability.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item quantity updated successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or quantity",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found in current cart",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient stock available",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CartResponse> updateItem(
            @Parameter(description = "UUID of the product to update", required = true)
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(productId, request));
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Remove a product from the shopping cart", description = "Removes a product entirely from the user's shopping cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product removed from cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found in current cart",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CartResponse> removeItem(
            @Parameter(description = "UUID of the product to remove", required = true)
            @PathVariable UUID productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(productId));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Clear the entire shopping cart", description = "Removes all items from the user's shopping cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Shopping cart cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
