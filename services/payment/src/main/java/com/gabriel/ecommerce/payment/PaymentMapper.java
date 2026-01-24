package com.gabriel.ecommerce.payment;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public class PaymentMapper {

    public Payment toPayment(@Valid PaymentRequest request) {
        return Payment.builder()
                .id(request.id())
                .orderId(request.id())
                .paymentMethod(request.paymentMethod())
                .amount(request.amount())
                .build();
    }
}
