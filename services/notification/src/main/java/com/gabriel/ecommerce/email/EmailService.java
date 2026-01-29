package com.gabriel.ecommerce.email;

import com.gabriel.ecommerce.kafka.order.Product;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendPaymentSuccessEmail(
            String destinationEmail,
            String customerName,
            BigDecimal amount,
            String orderReference
    ) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_RELATED,
                StandardCharsets.UTF_8.name());

        messageHelper.setFrom("email@email.com");

        final String templateName = EmailTemplates.PAYMENT_CONFIRMATION.getTemplate();

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("amount", amount);
        variables.put("orderReference", orderReference);

        Context context = new Context();
        context.setVariables(variables);

        messageHelper.setSubject(EmailTemplates.PAYMENT_CONFIRMATION.getSubject());

        try {
            String htmlContent = templateEngine.process(templateName, context);
            messageHelper.setText(htmlContent, true);

            messageHelper.setTo(destinationEmail);
            javaMailSender.send(message);
            log.info("Email sent successfully to {}", destinationEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", destinationEmail, e.getMessage(), e);
            throw e;
        }
    }

    @Async
    public void sendOrderConfirmationEmail(
            String destinationEmail,
            String customerName,
            BigDecimal amount,
            String orderReference,
            List<Product> products
    ) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_RELATED,
                StandardCharsets.UTF_8.name());

        messageHelper.setFrom("email@email.com");

        final String templateName = EmailTemplates.ORDER_CONFIRMATION.getTemplate();

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("totalAmount", amount);
        variables.put("orderReference", orderReference);
        variables.put("products", products);

        Context context = new Context();
        context.setVariables(variables);

        messageHelper.setSubject(EmailTemplates.ORDER_CONFIRMATION.getSubject());

        try {
            String htmlContent = templateEngine.process(templateName, context);
            messageHelper.setText(htmlContent, true);

            messageHelper.setTo(destinationEmail);
            javaMailSender.send(message);
            log.info("Email sent successfully to {}", destinationEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", destinationEmail, e.getMessage(), e);
            throw e;
        }
    }
}
