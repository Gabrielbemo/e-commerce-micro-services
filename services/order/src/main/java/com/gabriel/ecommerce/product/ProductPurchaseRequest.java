package com.gabriel.ecommerce.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ProductPurchaseRequest(
        @NotNull(message = "Product ID cannot be null")
        UUID productId,
        @Positive(message = "Quantity must be positive")
        double quantity
) {
}
