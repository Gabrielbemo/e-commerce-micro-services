package com.gabriel.ecommerce.payment;


import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        UUID id,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        UUID userId,
        String orderReference,
        Customer customer
) {
}
