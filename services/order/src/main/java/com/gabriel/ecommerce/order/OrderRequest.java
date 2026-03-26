package com.gabriel.ecommerce.order;

import com.gabriel.ecommerce.product.ProductPurchaseRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderRequest(
        @Schema(description = "Order id for updates", example = "018fbe57-cd79-7b3f-94b1-9bbf1ff6e9cb")
        UUID id,
        @Schema(description = "Business reference", example = "ORDER-001")
        String reference,
        @Positive(message = "Amount cannot be negative")
        @Schema(description = "Total amount", example = "2500.00")
        BigDecimal amount,
        @NotNull(message = "Payment method cannot be null")
        @Schema(description = "Payment method", example = "CREDIT_CARD")
        PaymentMethod paymentMethod,

        @NotNull(message = "Customer ID cannot be null")
        @Schema(description = "Customer identifier", example = "661f6f96cc6ce15f7fd8c5ba")
        String customerId,
        @NotEmpty(message = "Products cannot be empty")
        @Schema(description = "Products and quantities to purchase")
        List<ProductPurchaseRequest> productPurchaseRequests
) {
}
