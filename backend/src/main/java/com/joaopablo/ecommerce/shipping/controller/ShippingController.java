package com.joaopablo.ecommerce.shipping.controller;

import com.joaopablo.ecommerce.shipping.dto.request.CreateShippingRequest;
import com.joaopablo.ecommerce.shipping.dto.response.ShippingResponse;
import com.joaopablo.ecommerce.shipping.exception.ShippingNotFoundException;
import com.joaopablo.ecommerce.shipping.service.ShippingService;
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
@RequestMapping("/api/v1/shippings")
@RequiredArgsConstructor
@Tag(name = "Shipping Controller", description = "Endpoints for managing order shipments")
public class ShippingController {

    private final ShippingService service;

    @PostMapping
    @Operation(summary = "Create a shipping", description = "Creates a PROCESSING shipping for a CONFIRMED order with approved payment")
    public ResponseEntity<ShippingResponse> createShipping(@Valid @RequestBody CreateShippingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createShipping(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shipping by ID", description = "Fetches a specific shipping by its UUID")
    public ResponseEntity<ShippingResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get shipping by order ID", description = "Fetches the shipping associated with a given order")
    public ResponseEntity<ShippingResponse> findByOrderId(@PathVariable UUID orderId) {
        return service.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ShippingNotFoundException(
                        "Shipping not found for order id: " + orderId));
    }

    @GetMapping
    @Operation(summary = "List all shippings", description = "Returns all shippings in the system")
    public ResponseEntity<List<ShippingResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PatchMapping("/{id}/ship")
    @Operation(summary = "Mark as shipped", description = "Advances shipping status from PROCESSING to SHIPPED")
    public ResponseEntity<ShippingResponse> markAsShipped(@PathVariable UUID id) {
        return ResponseEntity.ok(service.markAsShipped(id));
    }

    @PatchMapping("/{id}/out-for-delivery")
    @Operation(summary = "Mark as out for delivery", description = "Advances shipping status from SHIPPED to OUT_FOR_DELIVERY")
    public ResponseEntity<ShippingResponse> markAsOutForDelivery(@PathVariable UUID id) {
        return ResponseEntity.ok(service.markAsOutForDelivery(id));
    }

    @PatchMapping("/{id}/deliver")
    @Operation(summary = "Mark as delivered", description = "Advances shipping status from OUT_FOR_DELIVERY to DELIVERED")
    public ResponseEntity<ShippingResponse> markAsDelivered(@PathVariable UUID id) {
        return ResponseEntity.ok(service.markAsDelivered(id));
    }
}
