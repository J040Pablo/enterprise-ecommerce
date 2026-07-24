package com.joaopablo.ecommerce.order.service;

import com.joaopablo.ecommerce.inventory.service.InventoryService;
import com.joaopablo.ecommerce.order.dto.request.CreateOrderRequest;
import com.joaopablo.ecommerce.order.dto.request.OrderItemRequest;
import com.joaopablo.ecommerce.order.dto.request.UpdateOrderStatusRequest;
import com.joaopablo.ecommerce.order.dto.response.OrderResponse;
import com.joaopablo.ecommerce.order.entity.Order;
import com.joaopablo.ecommerce.order.entity.OrderItem;
import com.joaopablo.ecommerce.order.entity.OrderStatus;
import com.joaopablo.ecommerce.order.exception.OrderNotFoundException;
import com.joaopablo.ecommerce.order.mapper.OrderMapper;
import com.joaopablo.ecommerce.order.repository.OrderRepository;
import com.joaopablo.ecommerce.product.dto.response.ProductResponse;
import com.joaopablo.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductResponse product = productService.findById(itemRequest.getProductId());
            
            // Decrease stock
            inventoryService.decreaseStock(itemRequest.getProductId(), itemRequest.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.id());
            orderItem.setProductName(product.name());
            orderItem.setUnitPrice(product.price());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.calculateSubtotal();

            order.addItem(orderItem);
        }

        order.calculateTotal();
        
        Order savedOrder = repository.save(order);
        return mapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        return mapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByUser(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = findEntityById(id);
        order.changeStatus(request.getStatus());
        
        return mapper.toResponse(repository.save(order));
    }

    @Transactional
    public void cancelOrder(UUID id) {
        Order order = findEntityById(id);
        order.changeStatus(OrderStatus.CANCELLED);
        
        // Em um sistema real, nós devolveríamos o estoque e geraríamos o pagamento reverso
        // Para este momento apenas mudamos o status
        // order.getItems().forEach(item -> inventoryService.increaseStock(item.getProductId(), item.getQuantity()));
        
        repository.save(order);
    }

    private Order findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
