package com.gabriel.ecommerce.orderline;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderLineMapperTest {

    private final OrderLineMapper orderLineMapper = new OrderLineMapper();

    @Test
    void shouldMapRequestToOrderLine() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderLineRequest request = new OrderLineRequest(id, orderId, "product-1", 2);

        OrderLine orderLine = orderLineMapper.toOrderLine(request);

        assertThat(orderLine.getId()).isEqualTo(id);
        assertThat(orderLine.getOrder().getId()).isEqualTo(orderId);
        assertThat(orderLine.getProductId()).isEqualTo("product-1");
        assertThat(orderLine.getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldMapOrderLineToResponse() {
        UUID id = UUID.randomUUID();
        OrderLine orderLine = OrderLine.builder().id(id).quantity(3).build();

        OrderLineResponse response = orderLineMapper.toOrderLineResponse(orderLine);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.quantity()).isEqualTo(3);
    }
}
