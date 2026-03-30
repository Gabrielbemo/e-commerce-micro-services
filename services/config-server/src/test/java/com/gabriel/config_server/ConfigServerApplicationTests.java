package com.gabriel.config_server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ConfigServerApplicationTests {

    @Test
    void shouldBeSpringBootApplicationAnnotated() {
        assertThat(ConfigServerApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
        assertThat(ConfigServerApplication.class.isAnnotationPresent(EnableConfigServer.class)).isTrue();
    }

    @Test
    void shouldDelegateMainToSpringApplication() {
        String[] args = new String[]{"--test"};
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

        try (var springApplicationMock = mockStatic(SpringApplication.class)) {
            springApplicationMock.when(() -> SpringApplication.run(ConfigServerApplication.class, args)).thenReturn(context);

            ConfigServerApplication.main(args);

            springApplicationMock.verify(() -> SpringApplication.run(ConfigServerApplication.class, args));
        }
    }

}
