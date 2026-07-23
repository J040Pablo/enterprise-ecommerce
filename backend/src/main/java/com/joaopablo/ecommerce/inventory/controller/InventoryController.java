package com.joaopablo.ecommerce.inventory.controller;

import com.joaopablo.ecommerce.inventory.dto.response.InventoryResponse;
import com.joaopablo.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {


    private final InventoryService service;



    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> findByProductId(
            @PathVariable UUID productId
    ) {

        return ResponseEntity.ok(
                service.findByProductId(productId)
        );

    }

    @PatchMapping("/{productId}/increase")
    public ResponseEntity<InventoryResponse> increaseStock(
            @PathVariable UUID productId,
            @RequestParam Integer quantity
    ) {

        return ResponseEntity.ok(
                service.increaseStock(productId, quantity)
        );

    }

    @PatchMapping("/{productId}/decrease")
    public ResponseEntity<InventoryResponse> decreaseStock(
            @PathVariable UUID productId,
            @RequestParam Integer quantity
    ) {

        return ResponseEntity.ok(
                service.decreaseStock(productId, quantity)
        );

    }

}