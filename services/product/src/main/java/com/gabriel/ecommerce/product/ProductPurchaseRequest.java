package com.gabriel.ecommerce.product;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductPurchaseRequest(
        @NotNull(message = "Product ID cannot be null")
        UUID productId,
        @NotNull(message = "Quantity cannot be null")
        double quantity
) {
}
