package com.gabriel.ecommerce.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
         @Schema(description = "Product id for updates", example = "018d2f1a-c101-7123-8234-a1b2c3d4e5f6")
         UUID id,
         @NotNull(message = "Name cannot be null")
         @Schema(description = "Product display name", example = "Gaming Laptop")
         String name,
         @NotNull(message = "Description cannot be null")
         @Schema(description = "Business description", example = "16-inch high performance laptop")
         String description,
         @Positive(message = "Available quantity must be positive")
         @Schema(description = "Available stock quantity", example = "12")
         double availableQuantity,
         @Positive(message = "Price must be positive")
         @Schema(description = "Unit price", example = "8999.90")
         BigDecimal price,
         @NotNull(message = "Category id cannot be null")
         @Schema(description = "Category identifier", example = "018d2f1a-c100-7000-9000-a1b2c3d4e5f6")
         UUID categoryId
) {
}
