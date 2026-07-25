package com.joaopablo.ecommerce.shipping.service;

import com.joaopablo.ecommerce.order.entity.Order;
import com.joaopablo.ecommerce.order.entity.OrderStatus;
import com.joaopablo.ecommerce.order.exception.OrderNotFoundException;
import com.joaopablo.ecommerce.order.repository.OrderRepository;
import com.joaopablo.ecommerce.payment.entity.Payment;
import com.joaopablo.ecommerce.payment.entity.PaymentStatus;
import com.joaopablo.ecommerce.payment.repository.PaymentRepository;
import com.joaopablo.ecommerce.shipping.dto.request.CreateShippingRequest;
import com.joaopablo.ecommerce.shipping.dto.response.ShippingResponse;
import com.joaopablo.ecommerce.shipping.entity.Shipping;
import com.joaopablo.ecommerce.shipping.entity.ShippingStatus;
import com.joaopablo.ecommerce.shipping.exception.InvalidShippingStatusException;
import com.joaopablo.ecommerce.shipping.exception.ShippingAlreadyExistsException;
import com.joaopablo.ecommerce.shipping.exception.ShippingCreationException;
import com.joaopablo.ecommerce.shipping.exception.ShippingNotFoundException;
import com.joaopablo.ecommerce.shipping.mapper.ShippingMapper;
import com.joaopablo.ecommerce.shipping.repository.ShippingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private ShippingMapper mapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ShippingService service;

    private UUID orderId;
    private UUID shippingId;
    private Order order;
    private Payment payment;
    private Shipping shipping;
    private CreateShippingRequest createRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        shippingId = UUID.randomUUID();

        order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CONFIRMED);

        payment = Payment.builder()
                .orderId(orderId)
                .status(PaymentStatus.APPROVED)
                .build();

        shipping = Shipping.builder()
                .orderId(orderId)
                .trackingCode("BR2026A91KD82")
                .carrier("CORREIOS")
                .status(ShippingStatus.PROCESSING)
                .estimatedDelivery(LocalDate.now().plusDays(5))
                .build();
        shipping.setId(shippingId);

        createRequest = CreateShippingRequest.builder()
                .orderId(orderId)
                .carrier("CORREIOS")
                .estimatedDelivery(LocalDate.now().plusDays(5))
                .build();
    }

    @Test
    void shouldCreateShippingSuccessfully() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(shippingRepository.existsByOrderId(orderId)).thenReturn(false);
        when(shippingRepository.existsByTrackingCode(anyString())).thenReturn(false);
        when(shippingRepository.save(any(Shipping.class))).thenAnswer(invocation -> {
            Shipping saved = invocation.getArgument(0);
            saved.setId(shippingId);
            return saved;
        });
        when(mapper.toResponse(any(Shipping.class))).thenReturn(
                ShippingResponse.builder()
                        .id(shippingId)
                        .orderId(orderId)
                        .status(ShippingStatus.PROCESSING)
                        .carrier("CORREIOS")
                        .build()
        );

        ShippingResponse response = service.createShipping(createRequest);

        assertEquals(ShippingStatus.PROCESSING, response.getStatus());
        assertEquals(orderId, response.getOrderId());

        ArgumentCaptor<Shipping> captor = ArgumentCaptor.forClass(Shipping.class);
        verify(shippingRepository).save(captor.capture());
        assertEquals(ShippingStatus.PROCESSING, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getTrackingCode());
        assertTrue(captor.getValue().getTrackingCode().startsWith("BR"));
    }

    @Test
    void shouldFindShippingById() {
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
        when(mapper.toResponse(shipping)).thenReturn(
                ShippingResponse.builder().id(shippingId).status(ShippingStatus.PROCESSING).build()
        );

        ShippingResponse response = service.findById(shippingId);

        assertEquals(shippingId, response.getId());
        assertEquals(ShippingStatus.PROCESSING, response.getStatus());
    }

    @Test
    void shouldFindShippingByOrderId() {
        when(shippingRepository.findByOrderId(orderId)).thenReturn(Optional.of(shipping));
        when(mapper.toResponse(shipping)).thenReturn(
                ShippingResponse.builder().id(shippingId).orderId(orderId).build()
        );

        ShippingResponse response = service.findByOrderId(orderId).orElseThrow();

        assertEquals(orderId, response.getOrderId());
    }

    @Test
    void shouldThrowWhenCreatingDuplicateShipping() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(shippingRepository.existsByOrderId(orderId)).thenReturn(true);

        assertThrows(ShippingAlreadyExistsException.class, () -> service.createShipping(createRequest));
        verify(shippingRepository, never()).save(any());
    }

    @Test
    void shouldAdvanceFromProcessingToShipped() {
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
        when(shippingRepository.save(shipping)).thenReturn(shipping);
        when(mapper.toResponse(shipping)).thenReturn(
                ShippingResponse.builder().id(shippingId).status(ShippingStatus.SHIPPED).build()
        );

        ShippingResponse response = service.markAsShipped(shippingId);

        assertEquals(ShippingStatus.SHIPPED, response.getStatus());
        assertEquals(ShippingStatus.SHIPPED, shipping.getStatus());
        assertNotNull(shipping.getShippedAt());
    }

    @Test
    void shouldAdvanceFromShippedToOutForDelivery() {
        shipping.setStatus(ShippingStatus.SHIPPED);
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
        when(shippingRepository.save(shipping)).thenReturn(shipping);
        when(mapper.toResponse(shipping)).thenReturn(
                ShippingResponse.builder().id(shippingId).status(ShippingStatus.OUT_FOR_DELIVERY).build()
        );

        ShippingResponse response = service.markAsOutForDelivery(shippingId);

        assertEquals(ShippingStatus.OUT_FOR_DELIVERY, response.getStatus());
        assertEquals(ShippingStatus.OUT_FOR_DELIVERY, shipping.getStatus());
    }

    @Test
    void shouldAdvanceFromOutForDeliveryToDelivered() {
        shipping.setStatus(ShippingStatus.OUT_FOR_DELIVERY);
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
        when(shippingRepository.save(shipping)).thenReturn(shipping);
        when(mapper.toResponse(shipping)).thenReturn(
                ShippingResponse.builder().id(shippingId).status(ShippingStatus.DELIVERED).build()
        );

        ShippingResponse response = service.markAsDelivered(shippingId);

        assertEquals(ShippingStatus.DELIVERED, response.getStatus());
        assertEquals(ShippingStatus.DELIVERED, shipping.getStatus());
        assertNotNull(shipping.getDeliveredAt());
    }

    @Test
    void shouldCancelProcessingShipping() {
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
        when(shippingRepository.save(shipping)).thenReturn(shipping);
        when(mapper.toResponse(shipping)).thenReturn(
                ShippingResponse.builder().id(shippingId).status(ShippingStatus.CANCELLED).build()
        );

        ShippingResponse response = service.cancelShipping(shippingId);

        assertEquals(ShippingStatus.CANCELLED, response.getStatus());
        assertEquals(ShippingStatus.CANCELLED, shipping.getStatus());
    }

    @Test
    void shouldThrowWhenCancellingAlreadyShippedShipping() {
        shipping.setStatus(ShippingStatus.SHIPPED);
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));

        assertThrows(InvalidShippingStatusException.class, () -> service.cancelShipping(shippingId));
        verify(shippingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrderIsCancelled() {
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(ShippingCreationException.class, () -> service.createShipping(createRequest));
        verify(shippingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.createShipping(createRequest));
        verify(shippingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrderIsNotConfirmed() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(ShippingCreationException.class, () -> service.createShipping(createRequest));
        verify(shippingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenGoingBackFromDeliveredToShipped() {
        shipping.setStatus(ShippingStatus.DELIVERED);
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));

        assertThrows(InvalidShippingStatusException.class, () -> service.markAsShipped(shippingId));
    }

    @Test
    void shouldThrowWhenSkippingStatus() {
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));

        assertThrows(InvalidShippingStatusException.class, () -> service.markAsDelivered(shippingId));
    }

    @Test
    void shouldThrowWhenShippingNotFound() {
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.empty());

        assertThrows(ShippingNotFoundException.class, () -> service.findById(shippingId));
    }

    @Test
    void shouldThrowWhenPaymentIsRejected() {
        payment.setStatus(PaymentStatus.REJECTED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        assertThrows(ShippingCreationException.class, () -> service.createShipping(createRequest));
        verify(shippingRepository, never()).save(any());
    }
}
