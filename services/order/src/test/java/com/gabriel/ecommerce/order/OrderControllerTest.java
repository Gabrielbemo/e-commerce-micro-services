package com.gabriel.ecommerce.order;

import com.gabriel.ecommerce.product.ProductPurchaseRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void shouldCreateOrder() {
        OrderRequest request = new OrderRequest(
                null,
                "ORDER-001",
                BigDecimal.TEN,
                PaymentMethod.CREDIT_CARD,
                "customer-1",
                List.of(new ProductPurchaseRequest("product-1", 1))
        );
        UUID orderId = UUID.randomUUID();
        when(orderService.createOrder(request)).thenReturn(orderId);

        ResponseEntity<UUID> response = orderController.createOrder(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderId);
    }

    @Test
    void shouldReturnAllOrders() {
        List<OrderResponse> orders = List.of(
                new OrderResponse(UUID.randomUUID(), "ORDER-001", BigDecimal.TEN, PaymentMethod.PAYPAL, "customer-1")
        );
        when(orderService.findAll()).thenReturn(orders);

        ResponseEntity<List<OrderResponse>> response = orderController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyElementsOf(orders);
    }

    @Test
    void shouldFindOrderById() {
        UUID orderId = UUID.randomUUID();
        OrderResponse order = new OrderResponse(orderId, "ORDER-001", BigDecimal.TEN, PaymentMethod.PAYPAL, "customer-1");
        when(orderService.findById(orderId)).thenReturn(order);

        ResponseEntity<OrderResponse> response = orderController.findById(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(order);
    }
}
