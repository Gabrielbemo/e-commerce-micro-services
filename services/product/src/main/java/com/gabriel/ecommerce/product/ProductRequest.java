package com.gabriel.ecommerce.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
         UUID id,
         @NotNull(message = "Name cannot be null")
         String name,
         @NotNull(message = "Description cannot be null")
         String description,
         @Positive(message = "Available quantity must be positive")
         double availableQuantity,
         @Positive(message = "Price must be positive")
         BigDecimal price,
         @NotNull(message = "Category id cannot be null")
         UUID categoryId
) {
}
