package com.joaopablo.ecommerce.order.controller;

import com.joaopablo.ecommerce.common.exception.ApiErrorResponse;
import com.joaopablo.ecommerce.order.dto.request.CreateOrderRequest;
import com.joaopablo.ecommerce.order.dto.request.UpdateOrderStatusRequest;
import com.joaopablo.ecommerce.order.dto.response.OrderResponse;
import com.joaopablo.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order lifecycle management — create, query, update status, and cancel orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(
            summary = "Place a new order",
            description = "Creates a new order, validates stock availability, and deducts inventory for each item."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order placed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — invalid items or missing fields",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict — insufficient stock for one or more items",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // [DEBUG] Confirma o que o backend recebeu após desserialização bem-sucedida
        log.info("[ORDER-DEBUG] POST /api/v1/orders recebido — userId={}, items({}): {}",
                request.getUserId(), request.getItems().size(), request.getItems());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOrder(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(
            summary = "Get order by ID",
            description = "Fetches a specific order and its items, payment, and shipping summary by UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> findById(
            @Parameter(description = "UUID of the order", example = "c3d4e5f6-0000-4bcd-9abc-222222222222", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all orders",
            description = "Returns all orders in the system. Restricted to admins."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order list returned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(
            summary = "List orders by user",
            description = "Returns all orders that belong to a specific user, identified by UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders found for user",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<OrderResponse>> findByUser(
            @Parameter(description = "UUID of the user", example = "d5e6f7a8-0000-4cde-aabc-333333333333", required = true)
            @PathVariable UUID userId) {
        return ResponseEntity.ok(service.findByUser(userId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update order status",
            description = "Transitions an order to a new status (e.g., PENDING → CONFIRMED → SHIPPED → DELIVERED)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> updateStatus(
            @Parameter(description = "UUID of the order", example = "c3d4e5f6-0000-4bcd-9abc-222222222222", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(
            summary = "Cancel an order",
            description = "Cancels an order if it has not yet been shipped. Stock is restored upon cancellation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled in its current state",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "UUID of the order to cancel", example = "c3d4e5f6-0000-4bcd-9abc-222222222222", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelOrder(id));
    }
}
