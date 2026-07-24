package com.joaopablo.ecommerce.payment.service;

import com.joaopablo.ecommerce.inventory.service.InventoryService;
import com.joaopablo.ecommerce.order.entity.Order;
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
import com.joaopablo.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper mapper;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Order order = findOrderById(request.getOrderId());
        return createPayment(order, request.getPaymentMethod());
    }

    @Transactional
    public PaymentResponse createPayment(Order order) {
        return createPayment(order, null);
    }

    private PaymentResponse createPayment(Order order, String paymentMethod) {
        ensureNoPaymentExists(order.getId());

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .paymentMethod(paymentMethod)
                .build();

        Payment saved = paymentRepository.save(payment);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(UUID id) {
        return mapper.toResponse(findPaymentById(id));
    }

    @Transactional
    public PaymentResponse approvePayment(UUID paymentId) {
        Payment payment = findPaymentById(paymentId);
        ensureStatus(payment, PaymentStatus.PENDING, "Only PENDING payments can be approved");

        payment.setStatus(PaymentStatus.APPROVED);

        Order order = findOrderById(payment.getOrderId());
        order.changeStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return mapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse rejectPayment(UUID paymentId) {
        Payment payment = findPaymentById(paymentId);
        ensureStatus(payment, PaymentStatus.PENDING, "Only PENDING payments can be rejected");

        payment.setStatus(PaymentStatus.REJECTED);

        Order order = findOrderById(payment.getOrderId());
        order.changeStatus(OrderStatus.CANCELLED);
        restoreInventory(order);
        orderRepository.save(order);

        return mapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse refundPayment(UUID paymentId) {
        Payment payment = findPaymentById(paymentId);
        ensureStatus(payment, PaymentStatus.APPROVED, "Only APPROVED payments can be refunded");

        payment.setStatus(PaymentStatus.REFUNDED);

        Order order = findOrderById(payment.getOrderId());
        order.changeStatus(OrderStatus.CANCELLED);
        restoreInventory(order);
        orderRepository.save(order);

        return mapper.toResponse(paymentRepository.save(payment));
    }

    private void restoreInventory(Order order) {
        order.getItems().forEach(item ->
                inventoryService.increaseStock(item.getProductId(), item.getQuantity())
        );
    }

    private void ensureNoPaymentExists(UUID orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(existing -> {
            throw new PaymentAlreadyExistsException(orderId);
        });
    }

    private void ensureStatus(Payment payment, PaymentStatus expected, String message) {
        if (payment.getStatus() != expected) {
            throw new InvalidPaymentStatusException(message + ". Current status: " + payment.getStatus());
        }
    }

    private Payment findPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private Order findOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
