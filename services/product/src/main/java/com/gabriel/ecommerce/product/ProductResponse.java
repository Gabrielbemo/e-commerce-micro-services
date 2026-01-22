package com.gabriel.ecommerce.product;

import java.math.BigDecimal;
import java.util.UUID;

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
