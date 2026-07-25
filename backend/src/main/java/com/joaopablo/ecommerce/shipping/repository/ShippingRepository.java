package com.joaopablo.ecommerce.shipping.repository;

import com.joaopablo.ecommerce.shipping.entity.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, UUID> {

    Optional<Shipping> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    boolean existsByTrackingCode(String trackingCode);
}
