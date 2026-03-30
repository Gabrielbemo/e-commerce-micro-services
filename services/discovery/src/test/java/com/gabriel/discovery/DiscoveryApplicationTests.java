package com.gabriel.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class DiscoveryApplicationTests {

    @Test
    void shouldBeSpringBootApplicationAnnotated() {
        assertThat(DiscoveryApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
        assertThat(DiscoveryApplication.class.isAnnotationPresent(EnableEurekaServer.class)).isTrue();
    }

    @Test
    void shouldDelegateMainToSpringApplication() {
        String[] args = new String[]{"--test"};
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

        try (var springApplicationMock = mockStatic(SpringApplication.class)) {
            springApplicationMock.when(() -> SpringApplication.run(DiscoveryApplication.class, args)).thenReturn(context);

            DiscoveryApplication.main(args);

            springApplicationMock.verify(() -> SpringApplication.run(DiscoveryApplication.class, args));
        }
    }

}
