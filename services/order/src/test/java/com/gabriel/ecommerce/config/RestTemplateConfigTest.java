package com.gabriel.ecommerce.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RestTemplateConfigTest {

    private final RestTemplateConfig restTemplateConfig = new RestTemplateConfig();

    @Test
    void shouldCreateRestTemplateBean() {
        RestTemplate restTemplate = restTemplateConfig.restTemplate();

        assertThat(restTemplate).isNotNull();
    }
}
