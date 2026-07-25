package com.joaopablo.ecommerce.shipping.dto.request;

import com.joaopablo.ecommerce.shipping.entity.ShippingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShippingStatusRequest {

    @NotNull(message = "Status is required")
    private ShippingStatus status;
}
