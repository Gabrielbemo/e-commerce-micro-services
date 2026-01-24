package com.gabriel.ecommerce.kafka;

import com.gabriel.ecommerce.customer.CustomerResponse;
import com.gabriel.ecommerce.order.PaymentMethod;
import com.gabriel.ecommerce.product.ProductPurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<ProductPurchaseResponse> products
) {
}
