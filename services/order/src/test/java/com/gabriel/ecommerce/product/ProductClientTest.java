package com.gabriel.ecommerce.product;

import com.gabriel.ecommerce.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductClient productClient;

    @Test
    void shouldPurchaseProducts() {
        ReflectionTestUtils.setField(productClient, "productUrl", "http://product-service/api/v1/products");
        List<ProductPurchaseRequest> request = List.of(new ProductPurchaseRequest("product-1", 2));
        List<ProductPurchaseResponse> responses = List.of(
                new ProductPurchaseResponse(UUID.randomUUID(), "Laptop", "Gaming", BigDecimal.TEN, 2)
        );

        when(restTemplate.exchange(
                eq("http://product-service/api/v1/products/purchase"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(responses));

        List<ProductPurchaseResponse> result = productClient.purchaseProducts(request);

        assertThat(result).containsExactlyElementsOf(responses);
    }

    @Test
    void shouldThrowWhenProductPurchaseRequestFails() {
        ReflectionTestUtils.setField(productClient, "productUrl", "http://product-service/api/v1/products");
        List<ProductPurchaseRequest> request = List.of(new ProductPurchaseRequest("product-1", 2));

        when(restTemplate.exchange(
                eq("http://product-service/api/v1/products/purchase"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));

        assertThatThrownBy(() -> productClient.purchaseProducts(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to purchase products");
    }
}
