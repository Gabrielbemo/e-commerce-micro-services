package com.gabriel.ecommerce.orderline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLineServiceTest {

    @Mock
    private OrderLineRepository orderLineRepository;
    @Mock
    private OrderLineMapper orderLineMapper;

    @InjectMocks
    private OrderLineService orderLineService;

    @Test
    void shouldSaveOrderLine() {
        UUID id = UUID.randomUUID();
        OrderLineRequest request = new OrderLineRequest(null, UUID.randomUUID(), "product-1", 2);
        OrderLine mapped = OrderLine.builder().productId("product-1").quantity(2).build();
        OrderLine saved = OrderLine.builder().id(id).build();

        when(orderLineMapper.toOrderLine(request)).thenReturn(mapped);
        when(orderLineRepository.save(mapped)).thenReturn(saved);

        UUID result = orderLineService.saveOrderLine(request);

        assertThat(result).isEqualTo(id);
    }

    @Test
    void shouldFindAllByOrderId() {
        UUID orderId = UUID.randomUUID();
        OrderLine orderLine = OrderLine.builder().id(UUID.randomUUID()).quantity(2).build();
        OrderLineResponse response = new OrderLineResponse(orderLine.getId(), 2);

        when(orderLineRepository.findAllByOrderId(orderId)).thenReturn(List.of(orderLine));
        when(orderLineMapper.toOrderLineResponse(orderLine)).thenReturn(response);

        List<OrderLineResponse> result = orderLineService.findAllByOrderId(orderId);

        assertThat(result).containsExactly(response);
        verify(orderLineRepository).findAllByOrderId(orderId);
    }
}
