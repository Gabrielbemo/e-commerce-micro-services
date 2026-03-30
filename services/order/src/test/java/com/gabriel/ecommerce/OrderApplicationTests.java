package com.gabriel.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class OrderApplicationTests {

    @Test
    void shouldBeSpringBootApplicationAnnotated() {
        assertThat(OrderApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }

}
