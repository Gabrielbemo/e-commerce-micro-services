package com.gabriel.ecommerce.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplatesTest {

    @Test
    void shouldExposePaymentTemplateMetadata() {
        assertThat(EmailTemplates.PAYMENT_CONFIRMATION.getTemplate()).isEqualTo("payment-confirmation.html");
        assertThat(EmailTemplates.PAYMENT_CONFIRMATION.getSubject()).isEqualTo("Payment successfully processed");
    }

    @Test
    void shouldExposeOrderTemplateMetadata() {
        assertThat(EmailTemplates.ORDER_CONFIRMATION.getTemplate()).isEqualTo("order-confirmation.html");
        assertThat(EmailTemplates.ORDER_CONFIRMATION.getSubject()).isEqualTo("Order confirmation");
    }
}
