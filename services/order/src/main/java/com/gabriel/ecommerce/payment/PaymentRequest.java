package com.gabriel.ecommerce.payment;

import com.gabriel.ecommerce.customer.CustomerResponse;
import com.gabriel.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        UUID userId,
        String orderReference,
        CustomerResponse customer
) {
}
