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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerClient customerClient;
    @Mock
    private ProductClient productClient;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderLineService orderLineService;
    @Mock
    private OrderProducer orderProducer;
    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        ProductPurchaseRequest firstProduct = new ProductPurchaseRequest("product-1", 2);
        ProductPurchaseRequest secondProduct = new ProductPurchaseRequest("product-2", 1);
        OrderRequest request = new OrderRequest(
                null,
                "ORDER-001",
                BigDecimal.valueOf(200),
                PaymentMethod.CREDIT_CARD,
                "customer-1",
                List.of(firstProduct, secondProduct)
        );
        CustomerResponse customer = new CustomerResponse("customer-1", "John", "Doe", "john@doe.com");
        List<ProductPurchaseResponse> purchasedProducts = List.of(
                new ProductPurchaseResponse(UUID.randomUUID(), "Laptop", "Gaming", BigDecimal.TEN, 2)
        );
        Order unsavedOrder = Order.builder().reference("ORDER-001").build();
        UUID orderId = UUID.randomUUID();
        Order savedOrder = Order.builder().id(orderId).reference("ORDER-001").build();

        when(customerClient.findCustomerById("customer-1")).thenReturn(Optional.of(customer));
        when(productClient.purchaseProducts(request.productPurchaseRequests())).thenReturn(purchasedProducts);
        when(orderMapper.toOrder(request)).thenReturn(unsavedOrder);
        when(orderRepository.save(unsavedOrder)).thenReturn(savedOrder);

        UUID result = orderService.createOrder(request);

        assertThat(result).isEqualTo(orderId);
        verify(orderLineService, times(2)).saveOrderLine(any(OrderLineRequest.class));

        ArgumentCaptor<PaymentRequest> paymentRequestCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(paymentClient).requestOderPayment(paymentRequestCaptor.capture());
        assertThat(paymentRequestCaptor.getValue().orderId()).isEqualTo(orderId);
        assertThat(paymentRequestCaptor.getValue().customer()).isEqualTo(customer);

        ArgumentCaptor<OrderConfirmation> orderConfirmationCaptor = ArgumentCaptor.forClass(OrderConfirmation.class);
        verify(orderProducer).sendOrderConfirmation(orderConfirmationCaptor.capture());
        assertThat(orderConfirmationCaptor.getValue().orderReference()).isEqualTo("ORDER-001");
        assertThat(orderConfirmationCaptor.getValue().products()).isEqualTo(purchasedProducts);
    }

    @Test
    void shouldThrowWhenCustomerDoesNotExist() {
        OrderRequest request = new OrderRequest(
                null,
                "ORDER-001",
                BigDecimal.valueOf(200),
                PaymentMethod.CREDIT_CARD,
                "customer-1",
                List.of(new ProductPurchaseRequest("product-1", 2))
        );
        when(customerClient.findCustomerById("customer-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Customer not found");

        verify(orderRepository, never()).save(any());
        verify(orderProducer, never()).sendOrderConfirmation(any());
    }

    @Test
    void shouldFindAllOrders() {
        Order order = Order.builder().id(UUID.randomUUID()).reference("ORDER-001").build();
        OrderResponse response = new OrderResponse(order.getId(), "ORDER-001", BigDecimal.TEN, PaymentMethod.PAYPAL, "customer-1");

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(response);

        List<OrderResponse> result = orderService.findAll();

        assertThat(result).containsExactly(response);
    }

    @Test
    void shouldFindOrderById() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).reference("ORDER-001").build();
        OrderResponse response = new OrderResponse(orderId, "ORDER-001", BigDecimal.TEN, PaymentMethod.PAYPAL, "customer-1");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(response);

        OrderResponse result = orderService.findById(orderId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(orderId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order not found");
    }
}
