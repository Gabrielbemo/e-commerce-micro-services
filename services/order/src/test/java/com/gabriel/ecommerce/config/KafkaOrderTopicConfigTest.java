package com.gabriel.ecommerce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaOrderTopicConfigTest {

    private final KafkaOrderTopicConfig kafkaOrderTopicConfig = new KafkaOrderTopicConfig();

    @Test
    void shouldCreateOrderTopic() {
        NewTopic topic = kafkaOrderTopicConfig.orderTopic();

        assertThat(topic.name()).isEqualTo("order-topic");
    }
}
