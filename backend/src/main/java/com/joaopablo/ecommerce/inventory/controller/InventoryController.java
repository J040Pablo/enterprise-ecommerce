package com.joaopablo.ecommerce.inventory.controller;

import com.joaopablo.ecommerce.common.exception.ApiErrorResponse;
import com.joaopablo.ecommerce.inventory.dto.response.InventoryResponse;
import com.joaopablo.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock management — query and adjust product stock levels")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {


    private final InventoryService service;



    @GetMapping("/{productId}")
    @Operation(
            summary = "Get inventory by product ID",
            description = "Returns the current stock record for the given product."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory record found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No inventory record for the given product",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<InventoryResponse> findByProductId(
            @Parameter(description = "UUID of the product", example = "a2e3c1b0-1234-4abc-8def-000000000001", required = true)
            @PathVariable UUID productId
    ) {

        return ResponseEntity.ok(
                service.findByProductId(productId)
        );

    }

    @PatchMapping("/{productId}/increase")
    @Operation(
            summary = "Increase product stock",
            description = "Adds the specified quantity to the product's current stock level."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock increased successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity — must be a positive integer",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<InventoryResponse> increaseStock(
            @Parameter(description = "UUID of the product", example = "a2e3c1b0-1234-4abc-8def-000000000001", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Number of units to add to current stock", example = "50", required = true)
            @RequestParam Integer quantity
    ) {

        return ResponseEntity.ok(
                service.increaseStock(productId, quantity)
        );

    }

    @PatchMapping("/{productId}/decrease")
    @Operation(
            summary = "Decrease product stock",
            description = "Subtracts the specified quantity from the product's current stock level."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock decreased successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<InventoryResponse> decreaseStock(
            @Parameter(description = "UUID of the product", example = "a2e3c1b0-1234-4abc-8def-000000000001", required = true)
            @PathVariable UUID productId,
            @Parameter(description = "Number of units to subtract from current stock", example = "10", required = true)
            @RequestParam Integer quantity
    ) {

        return ResponseEntity.ok(
                service.decreaseStock(productId, quantity)
        );

    }

}