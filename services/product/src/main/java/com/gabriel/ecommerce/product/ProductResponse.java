package com.gabriel.ecommerce.product;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductResponse(
        UUID id,
        String name,
        String description,
        double availableQuantity,
        BigDecimal price,
        UUID categoryId,
        String categoryName,
        String categoryDescription
) {
}
