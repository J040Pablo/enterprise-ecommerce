package com.joaopablo.ecommerce.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joaopablo.ecommerce.shipping.dto.request.CreateShippingRequest;
import com.joaopablo.ecommerce.shipping.dto.response.ShippingResponse;
import com.joaopablo.ecommerce.shipping.entity.ShippingStatus;
import com.joaopablo.ecommerce.shipping.service.ShippingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ShippingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShippingService service;

    @Test
    @WithMockUser
    void shouldCreateShipping() throws Exception {
        UUID orderId = UUID.randomUUID();
        CreateShippingRequest request = CreateShippingRequest.builder()
                .orderId(orderId)
                .carrier("CORREIOS")
                .estimatedDelivery(LocalDate.now().plusDays(5))
                .build();

        ShippingResponse response = ShippingResponse.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .trackingCode("BR2026A91KD82")
                .carrier("CORREIOS")
                .status(ShippingStatus.PROCESSING)
                .estimatedDelivery(request.getEstimatedDelivery())
                .build();

        when(service.createShipping(any(CreateShippingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/shippings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.carrier").value("CORREIOS"))
                .andExpect(jsonPath("$.trackingCode").value("BR2026A91KD82"));
    }

    @Test
    @WithMockUser
    void shouldFindShippingById() throws Exception {
        UUID shippingId = UUID.randomUUID();
        ShippingResponse response = ShippingResponse.builder()
                .id(shippingId)
                .status(ShippingStatus.PROCESSING)
                .build();

        when(service.findById(shippingId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/shippings/{id}", shippingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shippingId.toString()));
    }

    @Test
    @WithMockUser
    void shouldFindShippingByOrderId() throws Exception {
        UUID orderId = UUID.randomUUID();
        ShippingResponse response = ShippingResponse.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .status(ShippingStatus.PROCESSING)
                .build();

        when(service.findByOrderId(orderId)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/shippings/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    @WithMockUser
    void shouldListAllShippings() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                ShippingResponse.builder().id(UUID.randomUUID()).status(ShippingStatus.PROCESSING).build()
        ));

        mockMvc.perform(get("/api/v1/shippings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PROCESSING"));
    }

    @Test
    @WithMockUser
    void shouldMarkAsShipped() throws Exception {
        UUID shippingId = UUID.randomUUID();
        when(service.markAsShipped(eq(shippingId))).thenReturn(
                ShippingResponse.builder().id(shippingId).status(ShippingStatus.SHIPPED).build()
        );

        mockMvc.perform(patch("/api/v1/shippings/{id}/ship", shippingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    @WithMockUser
    void shouldMarkAsOutForDelivery() throws Exception {
        UUID shippingId = UUID.randomUUID();
        when(service.markAsOutForDelivery(eq(shippingId))).thenReturn(
                ShippingResponse.builder().id(shippingId).status(ShippingStatus.OUT_FOR_DELIVERY).build()
        );

        mockMvc.perform(patch("/api/v1/shippings/{id}/out-for-delivery", shippingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OUT_FOR_DELIVERY"));
    }

    @Test
    @WithMockUser
    void shouldMarkAsDelivered() throws Exception {
        UUID shippingId = UUID.randomUUID();
        when(service.markAsDelivered(eq(shippingId))).thenReturn(
                ShippingResponse.builder().id(shippingId).status(ShippingStatus.DELIVERED).build()
        );

        mockMvc.perform(patch("/api/v1/shippings/{id}/deliver", shippingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }
}
