package com.joaopablo.ecommerce.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joaopablo.ecommerce.payment.dto.request.CreatePaymentRequest;
import com.joaopablo.ecommerce.payment.dto.response.PaymentResponse;
import com.joaopablo.ecommerce.payment.entity.PaymentStatus;
import com.joaopablo.ecommerce.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.access.AccessDeniedException;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService service;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldCreatePayment() throws Exception {
        UUID orderId = UUID.randomUUID();
        CreatePaymentRequest request = new CreatePaymentRequest(orderId, "PIX");

        PaymentResponse response = PaymentResponse.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(150.00))
                .status(PaymentStatus.PENDING)
                .paymentMethod("PIX")
                .build();

        when(service.createPayment(any(CreatePaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentMethod").value("PIX"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerShouldGetForbiddenWhenCreatingPaymentForAnotherUsersOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        CreatePaymentRequest request = new CreatePaymentRequest(orderId, "PIX");

        when(service.createPayment(any(CreatePaymentRequest.class)))
                .thenThrow(new AccessDeniedException("Access denied to this resource"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminShouldCreatePayment() throws Exception {
        UUID orderId = UUID.randomUUID();
        CreatePaymentRequest request = new CreatePaymentRequest(orderId, "PIX");

        PaymentResponse response = PaymentResponse.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(150.00))
                .status(PaymentStatus.PENDING)
                .paymentMethod("PIX")
                .build();

        when(service.createPayment(any(CreatePaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldFindPaymentById() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .status(PaymentStatus.PENDING)
                .build();

        when(service.findById(paymentId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldApprovePayment() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .status(PaymentStatus.APPROVED)
                .build();

        when(service.approvePayment(eq(paymentId))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/payments/{id}/approve", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectPayment() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .status(PaymentStatus.REJECTED)
                .build();

        when(service.rejectPayment(eq(paymentId))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/payments/{id}/reject", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRefundPayment() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .status(PaymentStatus.REFUNDED)
                .build();

        when(service.refundPayment(eq(paymentId))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/payments/{id}/refund", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerShouldNotApprovePayment() throws Exception {
        UUID paymentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/payments/{id}/approve", paymentId))
                .andExpect(status().isForbidden());

        verify(service, never()).approvePayment(any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerShouldNotRejectPayment() throws Exception {
        UUID paymentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/payments/{id}/reject", paymentId))
                .andExpect(status().isForbidden());

        verify(service, never()).rejectPayment(any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerShouldNotRefundPayment() throws Exception {
        UUID paymentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/payments/{id}/refund", paymentId))
                .andExpect(status().isForbidden());

        verify(service, never()).refundPayment(any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerShouldGetForbiddenWhenAccessingAnotherUsersPayment() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(service.findById(paymentId)).thenThrow(new AccessDeniedException("Access denied to this resource"));

        mockMvc.perform(get("/api/v1/payments/{id}", paymentId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminShouldFindPaymentById() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .status(PaymentStatus.PENDING)
                .build();

        when(service.findById(paymentId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()));
    }
}
