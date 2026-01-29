package com.gabriel.ecommerce.orderline;

import java.util.UUID;

public record OrderLineRequest(
        UUID id,
        UUID orderId,
        String productId,
        double quantity
) {
}
