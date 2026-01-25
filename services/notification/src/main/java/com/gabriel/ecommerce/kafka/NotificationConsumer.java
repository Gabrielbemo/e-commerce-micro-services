package com.gabriel.ecommerce.kafka;

import com.gabriel.ecommerce.email.EmailService;
import com.gabriel.ecommerce.kafka.order.OrderConfirmation;
import com.gabriel.ecommerce.kafka.payment.PaymentConfirmation;
import com.gabriel.ecommerce.notification.Notification;
import com.gabriel.ecommerce.notification.NotificationRepository;
import com.gabriel.ecommerce.notification.NotificationType;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "payment-topic")
    public void consumePaymentSuccessNotification(PaymentConfirmation paymentConfirmation) throws MessagingException {
        log.info("Consuming payment success notification: {}", paymentConfirmation);
        notificationRepository.save(Notification.builder()
                .type(NotificationType.PAYMENT_CONFIRMATION)
                .notificationDate(Instant.now())
                .paymentConfirmation(paymentConfirmation)
                .build()
        );

        var customerName = paymentConfirmation.customerFirstName() + " " + paymentConfirmation.customerLastName();
        emailService.sendPaymentSuccessEmail(
                paymentConfirmation.customerEmail(),
                customerName,
                paymentConfirmation.amount(),
                paymentConfirmation.orderReference()
        );
    }

    @KafkaListener(topics = "order-topic")
    public void consumeOrderConfirmationNotification(OrderConfirmation orderConfirmation) throws MessagingException {
        log.info("Consuming order confirmation notification: {}", orderConfirmation);
        notificationRepository.save(Notification.builder()
                .type(NotificationType.ORDER_CONFIRMATION)
                .notificationDate(Instant.now())
                .orderConfirmation(orderConfirmation)
                .build()
        );

        var customerName = orderConfirmation.customer().firstName() + " " + orderConfirmation.customer().lastName();
        emailService.sendOrderConfirmationEmail(
                orderConfirmation.customer().email(),
                customerName,
                orderConfirmation.totalAmount(),
                orderConfirmation.orderReference(),
                orderConfirmation.products()
        );
    }
}
