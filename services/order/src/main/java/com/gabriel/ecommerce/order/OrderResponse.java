package com.gabriel.ecommerce.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String reference,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String customerId
) {
}
