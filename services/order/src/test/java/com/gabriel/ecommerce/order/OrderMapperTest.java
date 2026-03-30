package com.gabriel.ecommerce.order;

import com.gabriel.ecommerce.product.ProductPurchaseRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapper();

    @Test
    void shouldMapRequestToOrder() {
        OrderRequest request = new OrderRequest(
                UUID.randomUUID(),
                "ORDER-001",
                BigDecimal.valueOf(150),
                PaymentMethod.CREDIT_CARD,
                "customer-1",
                List.of(new ProductPurchaseRequest("product-1", 2))
        );

        Order order = orderMapper.toOrder(request);

        assertThat(order.getReference()).isEqualTo("ORDER-001");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("150");
        assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(order.getCustomerId()).isEqualTo("customer-1");
    }

    @Test
    void shouldMapOrderToResponse() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder()
                .id(id)
                .reference("ORDER-001")
                .totalAmount(BigDecimal.valueOf(150))
                .paymentMethod(PaymentMethod.PAYPAL)
                .customerId("customer-1")
                .build();

        OrderResponse response = orderMapper.toOrderResponse(order);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.reference()).isEqualTo("ORDER-001");
        assertThat(response.amount()).isEqualByComparingTo("150");
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.PAYPAL);
        assertThat(response.customerId()).isEqualTo("customer-1");
    }
}
