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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private static final String DEFAULT_CARRIER = "STANDARD";
    private static final int DEFAULT_DELIVERY_DAYS = 7;
    private static final String TRACKING_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final List<ShippingStatus> FORWARD_FLOW = List.of(
            ShippingStatus.PROCESSING,
            ShippingStatus.SHIPPED,
            ShippingStatus.OUT_FOR_DELIVERY,
            ShippingStatus.DELIVERED
    );

    private final ShippingRepository shippingRepository;
    private final ShippingMapper mapper;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public ShippingResponse createShipping(CreateShippingRequest request) {
        Order order = findOrderById(request.getOrderId());
        validateOrderForShipping(order);
        validatePaymentForShipping(order.getId());
        ensureNoShippingExists(order.getId());

        Shipping shipping = Shipping.builder()
                .orderId(order.getId())
                .trackingCode(generateUniqueTrackingCode())
                .carrier(request.getCarrier())
                .status(ShippingStatus.PROCESSING)
                .estimatedDelivery(request.getEstimatedDelivery())
                .build();

        Shipping saved = shippingRepository.save(shipping);
        return mapper.toResponse(saved);
    }

    @Transactional
    public ShippingResponse createShippingForApprovedOrder(UUID orderId) {
        CreateShippingRequest request = CreateShippingRequest.builder()
                .orderId(orderId)
                .carrier(DEFAULT_CARRIER)
                .estimatedDelivery(LocalDate.now().plusDays(DEFAULT_DELIVERY_DAYS))
                .build();

        return createShipping(request);
    }

    @Transactional(readOnly = true)
    public ShippingResponse findById(UUID id) {
        return mapper.toResponse(findShippingById(id));
    }

    @Transactional(readOnly = true)
    public Optional<ShippingResponse> findByOrderId(UUID orderId) {
        return shippingRepository.findByOrderId(orderId)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ShippingResponse> findAll() {
        return shippingRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShippingResponse markAsShipped(UUID id) {
        return advanceStatus(id, ShippingStatus.SHIPPED);
    }

    @Transactional
    public ShippingResponse markAsOutForDelivery(UUID id) {
        return advanceStatus(id, ShippingStatus.OUT_FOR_DELIVERY);
    }

    @Transactional
    public ShippingResponse markAsDelivered(UUID id) {
        return advanceStatus(id, ShippingStatus.DELIVERED);
    }

    @Transactional
    public ShippingResponse cancelShipping(UUID id) {
        Shipping shipping = findShippingById(id);
        cancel(shipping);
        return mapper.toResponse(shippingRepository.save(shipping));
    }

    @Transactional
    public void cancelByOrderId(UUID orderId) {
        shippingRepository.findByOrderId(orderId).ifPresent(shipping -> {
            cancel(shipping);
            shippingRepository.save(shipping);
        });
    }

    private ShippingResponse advanceStatus(UUID id, ShippingStatus target) {
        Shipping shipping = findShippingById(id);
        ensureForwardTransition(shipping, target);

        shipping.setStatus(target);

        if (target == ShippingStatus.SHIPPED) {
            shipping.setShippedAt(LocalDateTime.now());
        }

        if (target == ShippingStatus.DELIVERED) {
            shipping.setDeliveredAt(LocalDateTime.now());
        }

        return mapper.toResponse(shippingRepository.save(shipping));
    }

    private void cancel(Shipping shipping) {
        if (shipping.getStatus() != ShippingStatus.PROCESSING) {
            throw new InvalidShippingStatusException(
                    "Only PROCESSING shippings can be cancelled. Current status: " + shipping.getStatus()
            );
        }

        shipping.setStatus(ShippingStatus.CANCELLED);
    }

    private void ensureForwardTransition(Shipping shipping, ShippingStatus target) {
        ShippingStatus current = shipping.getStatus();

        if (current == ShippingStatus.CANCELLED || current == ShippingStatus.DELIVERED) {
            throw new InvalidShippingStatusException(
                    "Cannot change status from " + current + " to " + target
            );
        }

        int currentIndex = FORWARD_FLOW.indexOf(current);
        int targetIndex = FORWARD_FLOW.indexOf(target);

        if (currentIndex < 0 || targetIndex < 0 || targetIndex != currentIndex + 1) {
            throw new InvalidShippingStatusException(
                    "Invalid shipping status transition from " + current + " to " + target
            );
        }
    }

    private void validateOrderForShipping(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ShippingCreationException(
                    "Cannot create shipping for a CANCELLED order"
            );
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ShippingCreationException(
                    "Shipping can only be created for CONFIRMED orders. Current status: "
                            + order.getStatus()
            );
        }
    }

    private void validatePaymentForShipping(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShippingCreationException(
                        "Cannot create shipping: payment not found for order id: " + orderId
                ));

        if (payment.getStatus() == PaymentStatus.REJECTED) {
            throw new ShippingCreationException(
                    "Cannot create shipping for an order with REJECTED payment"
            );
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new ShippingCreationException(
                    "Cannot create shipping for an order with REFUNDED payment"
            );
        }

        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new ShippingCreationException(
                    "Cannot create shipping: payment must be APPROVED. Current status: "
                            + payment.getStatus()
            );
        }
    }

    private void ensureNoShippingExists(UUID orderId) {
        if (shippingRepository.existsByOrderId(orderId)) {
            throw new ShippingAlreadyExistsException(orderId);
        }
    }

    private String generateUniqueTrackingCode() {
        String code;

        do {
            code = generateTrackingCode();
        } while (shippingRepository.existsByTrackingCode(code));

        return code;
    }

    private String generateTrackingCode() {
        StringBuilder suffix = new StringBuilder(7);

        for (int i = 0; i < 7; i++) {
            suffix.append(TRACKING_ALPHABET.charAt(RANDOM.nextInt(TRACKING_ALPHABET.length())));
        }

        return "BR" + Year.now().getValue() + suffix;
    }

    private Shipping findShippingById(UUID id) {
        return shippingRepository.findById(id)
                .orElseThrow(() -> new ShippingNotFoundException(id));
    }

    private Order findOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}