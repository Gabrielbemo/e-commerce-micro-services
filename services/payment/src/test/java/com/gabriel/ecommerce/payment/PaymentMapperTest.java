package com.gabriel.ecommerce.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    private final PaymentMapper paymentMapper = new PaymentMapper();

    @Test
    void shouldMapRequestToPayment() {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentRequest request = new PaymentRequest(
                paymentId,
                BigDecimal.TEN,
                PaymentMethod.CREDIT_CARD,
                orderId,
                "ORDER-001",
                new Customer("customer-1", "John", "Doe", "john@doe.com")
        );

        Payment payment = paymentMapper.toPayment(request);

        assertThat(payment.getId()).isEqualTo(paymentId);
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualByComparingTo("10");
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }
}
