package com.gabriel.ecommerce.payment;


import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        @Schema(description = "Payment id for updates", example = "018fbe59-0f8e-74f8-844a-4eb76f37e141")
        UUID id,
        @Schema(description = "Amount paid", example = "2500.00")
        BigDecimal amount,
        @Schema(description = "Selected payment method", example = "CREDIT_CARD")
        PaymentMethod paymentMethod,
        @Schema(description = "Order identifier", example = "018fbe57-cd79-7b3f-94b1-9bbf1ff6e9cb")
        UUID orderId,
        @Schema(description = "Order business reference", example = "ORDER-001")
        String orderReference,
        @Schema(description = "Customer that placed the order")
        Customer customer
) {
}
