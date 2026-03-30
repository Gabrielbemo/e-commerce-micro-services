package com.gabriel.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationApplicationTests {

    @Test
    void shouldBeSpringBootApplicationAnnotated() {
        assertThat(NotificationApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }

}
