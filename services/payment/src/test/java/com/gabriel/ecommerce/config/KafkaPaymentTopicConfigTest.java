package com.gabriel.ecommerce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaPaymentTopicConfigTest {

    private final KafkaPaymentTopicConfig kafkaPaymentTopicConfig = new KafkaPaymentTopicConfig();

    @Test
    void shouldCreatePaymentTopic() {
        NewTopic topic = kafkaPaymentTopicConfig.paymentTopic();

        assertThat(topic.name()).isEqualTo("payment-topic");
    }
}
