package com.joaopablo.ecommerce.shipping.mapper;

import com.joaopablo.ecommerce.shipping.dto.response.ShippingResponse;
import com.joaopablo.ecommerce.shipping.entity.Shipping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShippingMapper {

    ShippingResponse toResponse(Shipping shipping);
}
