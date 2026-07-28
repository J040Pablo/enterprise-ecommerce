package com.joaopablo.ecommerce.payment.service;

import com.joaopablo.ecommerce.inventory.service.InventoryService;
import com.joaopablo.ecommerce.order.entity.Order;
import com.joaopablo.ecommerce.order.entity.OrderItem;
import com.joaopablo.ecommerce.order.entity.OrderStatus;
import com.joaopablo.ecommerce.order.exception.OrderNotFoundException;
import com.joaopablo.ecommerce.order.repository.OrderRepository;
import com.joaopablo.ecommerce.payment.dto.request.CreatePaymentRequest;
import com.joaopablo.ecommerce.payment.dto.response.PaymentResponse;
import com.joaopablo.ecommerce.payment.entity.Payment;
import com.joaopablo.ecommerce.payment.entity.PaymentStatus;
import com.joaopablo.ecommerce.payment.exception.InvalidPaymentStatusException;
import com.joaopablo.ecommerce.payment.exception.PaymentAlreadyExistsException;
import com.joaopablo.ecommerce.payment.exception.PaymentNotFoundException;
import com.joaopablo.ecommerce.payment.mapper.PaymentMapper;
import com.joaopablo.ecommerce.payment.messaging.PaymentEventPublisher;
import com.joaopablo.ecommerce.payment.repository.PaymentRepository;
import com.joaopablo.ecommerce.shipping.dto.response.ShippingResponse;
import com.joaopablo.ecommerce.shipping.entity.ShippingStatus;
import com.joaopablo.ecommerce.shipping.service.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper mapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ShippingService shippingService;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentService service;

    private UUID orderId;
    private UUID paymentId;
    private UUID productId;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        productId = UUID.randomUUID();

        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setProductName("Product A");
        item.setUnitPrice(BigDecimal.valueOf(50));
        item.setQuantity(2);
        item.calculateSubtotal();

        order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.addItem(item);

        payment = Payment.builder()
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100))
                .status(PaymentStatus.PENDING)
                .paymentMethod("CREDIT_CARD")
                .build();
        payment.setId(paymentId);
    }

    @Test
    void shouldCreatePaymentSuccessfully() {
        CreatePaymentRequest request = new CreatePaymentRequest(orderId, "CREDIT_CARD");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(paymentId);
            return saved;
        });

        PaymentResponse mapped = PaymentResponse.builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100))
                .status(PaymentStatus.PENDING)
                .paymentMethod("CREDIT_CARD")
                .build();
        when(mapper.toResponse(any(Payment.class))).thenReturn(mapped);

        PaymentResponse response = service.createPayment(request);

        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(orderId, captor.getValue().getOrderId());
        assertEquals(PaymentStatus.PENDING, captor.getValue().getStatus());
        assertEquals("CREDIT_CARD", captor.getValue().getPaymentMethod());
    }

    @Test
    void shouldCreatePaymentFromOrder() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(paymentId);
            return saved;
        });
        when(mapper.toResponse(any(Payment.class))).thenReturn(
                PaymentResponse.builder().id(paymentId).status(PaymentStatus.PENDING).build()
        );

        PaymentResponse response = service.createPayment(order);

        assertEquals(PaymentStatus.PENDING, response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldThrowWhenOrderNotFoundOnCreate() {
        CreatePaymentRequest request = new CreatePaymentRequest(orderId, "PIX");
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.createPayment(request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPaymentAlreadyExists() {
        CreatePaymentRequest request = new CreatePaymentRequest(orderId, "PIX");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        assertThrows(PaymentAlreadyExistsException.class, () -> service.createPayment(request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldApprovePaymentAndConfirmOrder() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(shippingService.createShippingForApprovedOrder(orderId)).thenReturn(
                ShippingResponse.builder().orderId(orderId).status(ShippingStatus.PROCESSING).build()
        );
        when(mapper.toResponse(payment)).thenReturn(
                PaymentResponse.builder().id(paymentId).status(PaymentStatus.APPROVED).build()
        );

        PaymentResponse response = service.approvePayment(paymentId);

        assertEquals(PaymentStatus.APPROVED, response.getStatus());
        assertEquals(PaymentStatus.APPROVED, payment.getStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository).save(order);
        verify(shippingService).createShippingForApprovedOrder(orderId);
        verify(paymentEventPublisher).publishPaymentApproved(payment);
    }

    @Test
    void shouldRejectPaymentCancelOrderAndRestoreStock() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(mapper.toResponse(payment)).thenReturn(
                PaymentResponse.builder().id(paymentId).status(PaymentStatus.REJECTED).build()
        );

        PaymentResponse response = service.rejectPayment(paymentId);

        assertEquals(PaymentStatus.REJECTED, response.getStatus());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(inventoryService).increaseStock(productId, 2);
        verify(orderRepository).save(order);
        verify(paymentEventPublisher).publishPaymentRejected(payment);
    }

    @Test
    void shouldRefundApprovedPayment() {
        payment.setStatus(PaymentStatus.APPROVED);
        order.setStatus(OrderStatus.CONFIRMED);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(mapper.toResponse(payment)).thenReturn(
                PaymentResponse.builder().id(paymentId).status(PaymentStatus.REFUNDED).build()
        );

        PaymentResponse response = service.refundPayment(paymentId);

        assertEquals(PaymentStatus.REFUNDED, response.getStatus());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(inventoryService).increaseStock(productId, 2);
        verify(shippingService).cancelByOrderId(orderId);
    }

    @Test
    void shouldThrowWhenRefundingNonApprovedPayment() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(InvalidPaymentStatusException.class, () -> service.refundPayment(paymentId));
        verify(inventoryService, never()).increaseStock(any(), any());
    }

    @Test
    void shouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> service.findById(paymentId));
    }
}
