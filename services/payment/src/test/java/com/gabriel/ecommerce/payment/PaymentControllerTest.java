package com.gabriel.ecommerce.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void shouldCreatePayment() {
        PaymentRequest request = new PaymentRequest(
                null,
                BigDecimal.valueOf(250),
                PaymentMethod.CREDIT_CARD,
                UUID.randomUUID(),
                "ORDER-001",
                new Customer("customer-1", "John", "Doe", "john@doe.com")
        );
        UUID paymentId = UUID.randomUUID();
        when(paymentService.createPayment(request)).thenReturn(paymentId);

        ResponseEntity<UUID> response = paymentController.createPayment(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(paymentId);
    }
}
