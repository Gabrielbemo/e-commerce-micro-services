package com.gabriel.ecommerce.email;

import com.gabriel.ecommerce.kafka.order.Product;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldSendPaymentSuccessEmail() throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("payment-confirmation.html"), any(Context.class))).thenReturn("<html>ok</html>");

        emailService.sendPaymentSuccessEmail("john@doe.com", "John Doe", BigDecimal.TEN, "ORDER-001");

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getSubject()).isEqualTo("Payment successfully processed");
        assertThat(messageCaptor.getValue().getAllRecipients()[0].toString()).isEqualTo("john@doe.com");
    }

    @Test
    void shouldSendOrderConfirmationEmail() throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("order-confirmation.html"), any(Context.class))).thenReturn("<html>ok</html>");

        emailService.sendOrderConfirmationEmail(
                "john@doe.com",
                "John Doe",
                BigDecimal.valueOf(99),
                "ORDER-002",
                List.of(new Product(UUID.randomUUID(), "Laptop", "Gaming", BigDecimal.TEN, 1))
        );

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getSubject()).isEqualTo("Order confirmation");
        assertThat(messageCaptor.getValue().getAllRecipients()[0].toString()).isEqualTo("john@doe.com");
    }
}
