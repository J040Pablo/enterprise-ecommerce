package com.joaopablo.ecommerce.cart.entity;

import com.joaopablo.ecommerce.common.entity.BaseEntity;
import com.joaopablo.ecommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cart_product", columnNames = {"cart_id", "product_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem cartItem = (CartItem) o;
        return getId() != null && getId().equals(cartItem.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public java.math.BigDecimal getSubtotal() {
        if (product == null || product.getPrice() == null || quantity == null) {
            return java.math.BigDecimal.ZERO;
        }
        return product.getPrice().multiply(java.math.BigDecimal.valueOf(quantity));
    }
}
