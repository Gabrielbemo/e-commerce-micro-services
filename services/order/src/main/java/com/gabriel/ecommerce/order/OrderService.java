package com.gabriel.ecommerce.order;

import com.gabriel.ecommerce.customer.CustomerClient;
import com.gabriel.ecommerce.exception.BusinessException;
import com.gabriel.ecommerce.orderline.OrderLineRequest;
import com.gabriel.ecommerce.orderline.OrderLineService;
import com.gabriel.ecommerce.product.ProductClient;
import com.gabriel.ecommerce.product.ProductPurchaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;
    private final OrderLineService orderLineService;

    public UUID createOrder(@Valid OrderRequest request) {
    //OpenFeign
    var customer = this.customerClient.findCustomerById(request.customerId())
            .orElseThrow(() -> new BusinessException("Customer not found"));

    //(RestTemplate)
    this.productClient.purchaseProducts(request.productPurchaseRequests());

    var order = this.orderRepository.save(orderMapper.toOrder(request));

    for (ProductPurchaseRequest productPurchaseRequest : request.productPurchaseRequests()) {
        orderLineService.saveOrderLine(
                new OrderLineRequest(
                        null,
                        order.getId(),
                        productPurchaseRequest.productId(),
                        productPurchaseRequest.quantity()
                )
        );
    }

    // start payment process

    // send the order confirmation -
    return null;
    }
}
