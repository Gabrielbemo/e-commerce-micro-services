package com.gabriel.ecommerce.orderline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLineControllerTest {

    @Mock
    private OrderLineService orderLineService;

    @InjectMocks
    private OrderLineController orderLineController;

    @Test
    void shouldFindOrderLinesByOrderId() {
        UUID orderId = UUID.randomUUID();
        List<OrderLineResponse> lines = List.of(new OrderLineResponse(UUID.randomUUID(), 2));
        when(orderLineService.findAllByOrderId(orderId)).thenReturn(lines);

        ResponseEntity<List<OrderLineResponse>> response = orderLineController.findAllByOrderId(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyElementsOf(lines);
    }
}
