package com.gabriel.ecommerce.product;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ProductPurchaseRequest(
        @NotNull(message = "Product ID cannot be null")
        @Schema(description = "Product identifier", example = "018d2f1a-c101-7123-8234-a1b2c3d4e5f6")
        UUID productId,
        @NotNull(message = "Quantity cannot be null")
        @Schema(description = "Requested quantity", example = "1")
        double quantity
) {
}
