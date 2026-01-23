package com.gabriel.ecommerce.order;

import com.gabriel.ecommerce.product.ProductPurchaseRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderRequest(
    UUID id,
    String reference,
    @Positive(message = "Amount cannot be negative")
    BigDecimal amount,
    @NotNull(message = "Payment method cannot be null")
    PaymentMethod paymentMethod,

    @NotNull(message = "Customer ID cannot be null")
    UUID customerId,
    @NotEmpty(message = "Products cannot be empty")
    List<ProductPurchaseRequest> productPurchaseRequests
) {
}
