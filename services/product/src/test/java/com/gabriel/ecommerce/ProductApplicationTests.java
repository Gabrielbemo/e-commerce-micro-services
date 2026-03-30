package com.gabriel.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class ProductApplicationTests {

    @Test
    void shouldBeSpringBootApplicationAnnotated() {
        assertThat(ProductApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }

}
