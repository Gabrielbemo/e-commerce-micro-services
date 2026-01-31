package com.gabriel.ecommerce.order;

import com.gabriel.ecommerce.customer.CustomerClient;
import com.gabriel.ecommerce.customer.CustomerResponse;
import com.gabriel.ecommerce.exception.BusinessException;
import com.gabriel.ecommerce.kafka.OrderConfirmation;
import com.gabriel.ecommerce.kafka.OrderProducer;
import com.gabriel.ecommerce.orderline.OrderLineRequest;
import com.gabriel.ecommerce.orderline.OrderLineService;
import com.gabriel.ecommerce.payment.PaymentClient;
import com.gabriel.ecommerce.payment.PaymentRequest;
import com.gabriel.ecommerce.product.ProductClient;
import com.gabriel.ecommerce.product.ProductPurchaseRequest;
import com.gabriel.ecommerce.product.ProductPurchaseResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;
    private final PaymentClient paymentClient;

    public UUID createOrder(@Valid OrderRequest request) {
        //OpenFeign
        var customer = this.customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Customer not found"));

        //(RestTemplate)
        var purchasedProducts = this.productClient.purchaseProducts(request.productPurchaseRequests());

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

        processPayment(request, order, customer);

        sendOrderConfirmationNotification(request, customer, purchasedProducts);

        return order.getId();
    }

    private void processPayment(OrderRequest request, Order order, CustomerResponse customer) {
        paymentClient.requestOderPayment(new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        ));
    }

    private void sendOrderConfirmationNotification(OrderRequest request, CustomerResponse customer, List<ProductPurchaseResponse> purchasedProducts) {
        orderProducer.sendOrderConfirmation(new OrderConfirmation(
                request.reference(),
                request.amount(),
                request.paymentMethod(),
                customer,
                purchasedProducts
        ));
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    public OrderResponse findById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::toOrderResponse)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }
}
