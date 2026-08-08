package com.joaopablo.ecommerce.payment.controller;

import com.joaopablo.ecommerce.common.exception.ApiErrorResponse;
import com.joaopablo.ecommerce.payment.dto.request.CreatePaymentRequest;
import com.joaopablo.ecommerce.payment.dto.response.PaymentResponse;
import com.joaopablo.ecommerce.payment.service.PaymentService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment lifecycle management — create, approve, reject, and refund payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(
            summary = "Create a payment",
            description = "Creates a PENDING payment linked to an existing confirmed order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — invalid or missing fields",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(
            summary = "Get payment by ID",
            description = "Fetches a specific payment and its current status by UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PaymentResponse> findById(
            @Parameter(description = "UUID of the payment", example = "e7f8a9b0-0000-4efg-bbcd-444444444444", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Approve payment",
            description = "Approves a PENDING payment and automatically confirms the associated order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment approved and order confirmed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payment is not in a PENDING state",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PaymentResponse> approvePayment(
            @Parameter(description = "UUID of the payment to approve", example = "e7f8a9b0-0000-4efg-bbcd-444444444444", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.approvePayment(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reject payment",
            description = "Rejects a PENDING payment, cancels the order, and restores product inventory."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment rejected and order cancelled",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payment is not in a PENDING state",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PaymentResponse> rejectPayment(
            @Parameter(description = "UUID of the payment to reject", example = "e7f8a9b0-0000-4efg-bbcd-444444444444", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.rejectPayment(id));
    }

    @PatchMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Refund payment",
            description = "Refunds an APPROVED payment, cancels the order, and restores product inventory."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment refunded and order cancelled",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payment is not in an APPROVED state",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PaymentResponse> refundPayment(
            @Parameter(description = "UUID of the payment to refund", example = "e7f8a9b0-0000-4efg-bbcd-444444444444", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.refundPayment(id));
    }
}
