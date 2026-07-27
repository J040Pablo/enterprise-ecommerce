package com.joaopablo.ecommerce.order.dto.request;

import com.joaopablo.ecommerce.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "UpdateOrderStatusRequest", description = "Payload for transitioning an order to a new status")
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(
            description = "Target status for the order. Valid transitions depend on the current status.",
            example = "SHIPPED",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OrderStatus status;

}
