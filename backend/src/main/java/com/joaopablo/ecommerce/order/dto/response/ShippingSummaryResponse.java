package com.joaopablo.ecommerce.order.dto.response;

import com.joaopablo.ecommerce.shipping.entity.ShippingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingSummaryResponse {

    private UUID id;
    private String trackingCode;
    private ShippingStatus status;

}
