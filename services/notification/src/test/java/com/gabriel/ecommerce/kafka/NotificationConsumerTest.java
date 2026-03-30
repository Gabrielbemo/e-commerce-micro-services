package com.gabriel.ecommerce.kafka;

import com.gabriel.ecommerce.email.EmailService;
import com.gabriel.ecommerce.kafka.order.Customer;
import com.gabriel.ecommerce.kafka.order.OrderConfirmation;
import com.gabriel.ecommerce.kafka.order.Product;
import com.gabriel.ecommerce.kafka.payment.PaymentConfirmation;
import com.gabriel.ecommerce.kafka.payment.PaymentMethod;
import com.gabriel.ecommerce.notification.Notification;
import com.gabriel.ecommerce.notification.NotificationRepository;
import com.gabriel.ecommerce.notification.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    void shouldConsumePaymentSuccessNotification() throws Exception {
        PaymentConfirmation paymentConfirmation = new PaymentConfirmation(
                "ORDER-001",
                BigDecimal.valueOf(120),
                PaymentMethod.CREDIT_CARD,
                "John",
                "Doe",
                "john@doe.com"
        );

        notificationConsumer.consumePaymentSuccessNotification(paymentConfirmation);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getType()).isEqualTo(NotificationType.PAYMENT_CONFIRMATION);
        assertThat(notificationCaptor.getValue().getPaymentConfirmation()).isEqualTo(paymentConfirmation);

        verify(emailService).sendPaymentSuccessEmail("john@doe.com", "John Doe", BigDecimal.valueOf(120), "ORDER-001");
    }

    @Test
    void shouldConsumeOrderConfirmationNotification() throws Exception {
        OrderConfirmation orderConfirmation = new OrderConfirmation(
                "ORDER-002",
                BigDecimal.valueOf(300),
                PaymentMethod.PAYPAL,
                new Customer("customer-1", "Jane", "Doe", "jane@doe.com"),
                List.of(new Product(UUID.randomUUID(), "Laptop", "Gaming", BigDecimal.TEN, 1))
        );

        notificationConsumer.consumeOrderConfirmationNotification(orderConfirmation);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getType()).isEqualTo(NotificationType.ORDER_CONFIRMATION);
        assertThat(notificationCaptor.getValue().getOrderConfirmation()).isEqualTo(orderConfirmation);

        verify(emailService).sendOrderConfirmationEmail(
                "jane@doe.com",
                "Jane Doe",
                BigDecimal.valueOf(300),
                "ORDER-002",
                orderConfirmation.products()
        );
    }
}
