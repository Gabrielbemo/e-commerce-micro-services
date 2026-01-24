package com.gabriel.ecommerce.orderline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderLineService {
    private final OrderLineRepository orderLineRepository;
    private final OrderLineMapper orderLineMapper;

    public UUID saveOrderLine(OrderLineRequest orderLineRequest) {
        var orderLine = orderLineMapper.toOrderLine(orderLineRequest);
        return orderLineRepository.save(orderLine).getId();
    }

    public List<OrderLineResponse> findAllByOrderId(UUID orderId) {
        return orderLineRepository.findAllByOrderId(orderId).stream()
                .map(orderLineMapper::toOrderLineResponse)
                .toList();
    }
}
