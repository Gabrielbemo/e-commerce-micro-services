package com.gabriel.ecommerce.payment;

import com.gabriel.ecommerce.notification.NotificationProducer;
import com.gabriel.ecommerce.notification.PaymentNotificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationProducer notificationProducer;

    public UUID createPayment(@Valid PaymentRequest request) {
        var payment = paymentRepository.save(paymentMapper.toPayment(request));
        notificationProducer.sendNotification(new PaymentNotificationRequest(
                request.orderReference(),
                request.amount(),
                request.paymentMethod(),
                request.customer().firstName(),
                request.customer().lastName(),
                request.customer().email()
        ));
        return payment.getId();
    }
}
