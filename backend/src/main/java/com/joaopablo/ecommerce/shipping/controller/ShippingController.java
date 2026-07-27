package com.joaopablo.ecommerce.shipping.controller;

import com.joaopablo.ecommerce.common.exception.ApiErrorResponse;
import com.joaopablo.ecommerce.shipping.dto.request.CreateShippingRequest;
import com.joaopablo.ecommerce.shipping.dto.response.ShippingResponse;
import com.joaopablo.ecommerce.shipping.exception.ShippingNotFoundException;
import com.joaopablo.ecommerce.shipping.service.ShippingService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shippings")
@RequiredArgsConstructor
@Tag(name = "Shipping", description = "Shipment lifecycle management — create and track order deliveries")
@SecurityRequirement(name = "bearerAuth")
public class ShippingController {

    private final ShippingService service;

    @PostMapping
    @Operation(
            summary = "Create a shipping",
            description = "Creates a shipping record in PROCESSING status for a CONFIRMED order with an approved payment."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Shipping created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShippingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or order not eligible for shipping",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ShippingResponse> createShipping(@Valid @RequestBody CreateShippingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createShipping(request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get shipping by ID",
            description = "Fetches a specific shipping record and its tracking information by UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shipping record found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShippingResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Shipping not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ShippingResponse> findById(
            @Parameter(description = "UUID of the shipping record", example = "f0a1b2c3-0000-4fgh-ccde-555555555555", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/order/{orderId}")
    @Operation(
            summary = "Get shipping by order ID",
            description = "Fetches the shipping record associated with a given order UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shipping record found for the order",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShippingResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No shipping found for the given order",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ShippingResponse> findByOrderId(
            @Parameter(description = "UUID of the order", example = "c3d4e5f6-0000-4bcd-9abc-222222222222", required = true)
            @PathVariable UUID orderId) {
        return service.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ShippingNotFoundException(
                        "Shipping not found for order id: " + orderId));
    }

    @GetMapping
    @Operation(
            summary = "List all shippings",
            description = "Returns all shipping records in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shipping list returned",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<ShippingResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PatchMapping("/{id}/ship")
    @Operation(
            summary = "Mark as shipped",
            description = "Advances shipping status from PROCESSING to SHIPPED and records the shipped timestamp."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated to SHIPPED",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShippingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Shipping not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ShippingResponse> markAsShipped(
            @Parameter(description = "UUID of the shipping record", example = "f0a1b2c3-0000-4fgh-ccde-555555555555", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.markAsShipped(id));
    }

    @PatchMapping("/{id}/out-for-delivery")
    @Operation(
            summary = "Mark as out for delivery",
            description = "Advances shipping status from SHIPPED to OUT_FOR_DELIVERY."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated to OUT_FOR_DELIVERY",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShippingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Shipping not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ShippingResponse> markAsOutForDelivery(
            @Parameter(description = "UUID of the shipping record", example = "f0a1b2c3-0000-4fgh-ccde-555555555555", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.markAsOutForDelivery(id));
    }

    @PatchMapping("/{id}/deliver")
    @Operation(
            summary = "Mark as delivered",
            description = "Advances shipping status from OUT_FOR_DELIVERY to DELIVERED and records the delivered timestamp."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated to DELIVERED",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShippingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Shipping not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ShippingResponse> markAsDelivered(
            @Parameter(description = "UUID of the shipping record", example = "f0a1b2c3-0000-4fgh-ccde-555555555555", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.markAsDelivered(id));
    }
}
