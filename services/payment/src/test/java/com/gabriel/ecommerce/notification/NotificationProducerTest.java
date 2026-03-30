package com.gabriel.ecommerce.notification;

import com.gabriel.ecommerce.payment.PaymentMethod;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    @Mock
    private KafkaTemplate<String, PaymentNotificationRequest> kafkaTemplate;

    @InjectMocks
    private NotificationProducer notificationProducer;

    @Test
    void shouldSendPaymentNotificationMessage() {
        PaymentNotificationRequest request = new PaymentNotificationRequest(
                "ORDER-001",
                BigDecimal.valueOf(120),
                PaymentMethod.CREDIT_CARD,
                "John",
                "Doe",
                "john@doe.com"
        );

        notificationProducer.sendNotification(request);

        ArgumentCaptor<Message<PaymentNotificationRequest>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getPayload()).isEqualTo(request);
        assertThat(messageCaptor.getValue().getHeaders().get(KafkaHeaders.TOPIC)).isEqualTo("payment-topic");
    }
}
