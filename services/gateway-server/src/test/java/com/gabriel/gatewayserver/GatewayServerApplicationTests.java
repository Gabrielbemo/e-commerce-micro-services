package com.gabriel.gatewayserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayServerApplicationTests {

    @Test
    void shouldBeSpringBootApplicationAnnotated() {
        assertThat(GatewayServerApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }

}
