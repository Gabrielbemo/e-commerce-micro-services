package com.gabriel.ecommerce.payment;


import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        UUID id,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        UUID orderId,
        String orderReference,
        Customer customer
) {
}
