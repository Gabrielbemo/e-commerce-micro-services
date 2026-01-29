package com.gabriel.ecommerce.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductPurchaseRequest(
        @NotNull(message = "Product ID cannot be null")
        String productId,
        @Positive(message = "Quantity must be positive")
        double quantity
) {
}
