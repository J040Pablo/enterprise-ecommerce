package com.joaopablo.ecommerce.order.controller;

import com.joaopablo.ecommerce.order.dto.request.CreateOrderRequest;
import com.joaopablo.ecommerce.order.dto.request.UpdateOrderStatusRequest;
import com.joaopablo.ecommerce.order.dto.response.OrderResponse;
import com.joaopablo.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Endpoints for managing user orders")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates an order and validates/deducts stock")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOrder(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Fetches a specific order by its UUID")
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    @Operation(summary = "List all orders", description = "Fetches a list of all orders")
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List user orders", description = "Returns all orders that belong to a specific user")
    public ResponseEntity<List<OrderResponse>> findByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.findByUser(userId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Changes an existing order's status (e.g., to SHIPPED or DELIVERED)")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order", description = "Cancels an order if it's in a valid status to do so")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@PathVariable UUID id) {
        service.cancelOrder(id);
    }
}
