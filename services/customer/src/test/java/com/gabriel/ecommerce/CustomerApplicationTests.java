package com.gabriel.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerApplicationTests {

    @Test
    void shouldBeSpringBootApplicationAnnotated() {
        assertThat(CustomerApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }

}
