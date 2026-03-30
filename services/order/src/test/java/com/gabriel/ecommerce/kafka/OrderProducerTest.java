package com.gabriel.ecommerce.kafka;

import com.gabriel.ecommerce.customer.CustomerResponse;
import com.gabriel.ecommerce.order.PaymentMethod;
import com.gabriel.ecommerce.product.ProductPurchaseResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderProducerTest {

    @Mock
    private KafkaTemplate<String, OrderConfirmation> kafkaTemplate;

    @InjectMocks
    private OrderProducer orderProducer;

    @Test
    void shouldSendOrderConfirmationMessage() {
        OrderConfirmation orderConfirmation = new OrderConfirmation(
                "ORDER-001",
                BigDecimal.valueOf(200),
                PaymentMethod.CREDIT_CARD,
                new CustomerResponse("customer-1", "John", "Doe", "john@doe.com"),
                List.of(new ProductPurchaseResponse(UUID.randomUUID(), "Laptop", "Gaming", BigDecimal.TEN, 2))
        );

        orderProducer.sendOrderConfirmation(orderConfirmation);

        ArgumentCaptor<Message<OrderConfirmation>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getPayload()).isEqualTo(orderConfirmation);
        assertThat(messageCaptor.getValue().getHeaders().get(KafkaHeaders.TOPIC)).isEqualTo("order-topic");
    }
}
