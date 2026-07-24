package com.joaopablo.ecommerce.payment.controller;

import com.joaopablo.ecommerce.payment.dto.request.CreatePaymentRequest;
import com.joaopablo.ecommerce.payment.dto.response.PaymentResponse;
import com.joaopablo.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Endpoints for managing order payments")
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    @Operation(summary = "Create a payment", description = "Creates a PENDING payment for an existing order")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Fetches a specific payment by its UUID")
    public ResponseEntity<PaymentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve payment", description = "Approves a payment and confirms the related order")
    public ResponseEntity<PaymentResponse> approvePayment(@PathVariable UUID id) {
        return ResponseEntity.ok(service.approvePayment(id));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject payment", description = "Rejects a payment, cancels the order and restores inventory")
    public ResponseEntity<PaymentResponse> rejectPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(service.rejectPayment(id));
    }

    @PatchMapping("/{id}/refund")
    @Operation(summary = "Refund payment", description = "Refunds an approved payment, cancels the order and restores inventory")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(service.refundPayment(id));
    }
}
