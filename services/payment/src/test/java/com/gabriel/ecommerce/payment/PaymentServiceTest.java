package com.gabriel.ecommerce.payment;

import com.gabriel.ecommerce.notification.NotificationProducer;
import com.gabriel.ecommerce.notification.PaymentNotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldCreatePaymentAndSendNotification() {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentRequest request = new PaymentRequest(
                null,
                BigDecimal.valueOf(250),
                PaymentMethod.CREDIT_CARD,
                orderId,
                "ORDER-001",
                new Customer("customer-1", "John", "Doe", "john@doe.com")
        );
        Payment mapped = Payment.builder().orderId(orderId).build();
        Payment saved = Payment.builder().id(paymentId).build();

        when(paymentMapper.toPayment(request)).thenReturn(mapped);
        when(paymentRepository.save(mapped)).thenReturn(saved);

        UUID result = paymentService.createPayment(request);

        assertThat(result).isEqualTo(paymentId);

        ArgumentCaptor<PaymentNotificationRequest> notificationCaptor = ArgumentCaptor.forClass(PaymentNotificationRequest.class);
        verify(notificationProducer).sendNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().orderReference()).isEqualTo("ORDER-001");
        assertThat(notificationCaptor.getValue().customerEmail()).isEqualTo("john@doe.com");
    }
}
