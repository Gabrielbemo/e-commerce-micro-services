package com.gabriel.ecommerce.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void shouldCreateProduct() {
        ProductRequest request = new ProductRequest(null, "Laptop", "Gaming", 10, BigDecimal.TEN, UUID.randomUUID());
        UUID createdId = UUID.randomUUID();
        when(productService.createProduct(request)).thenReturn(createdId);

        ResponseEntity<UUID> response = productController.createProduct(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(createdId);
    }

    @Test
    void shouldPurchaseProducts() {
        UUID productId = UUID.randomUUID();
        List<ProductPurchaseRequest> requests = List.of(new ProductPurchaseRequest(productId, 1));
        List<ProductPurchaseResponse> serviceResponse = List.of(
                new ProductPurchaseResponse(productId, "Laptop", "Gaming", BigDecimal.TEN, 1)
        );
        when(productService.purchaseProducts(requests)).thenReturn(serviceResponse);

        ResponseEntity<List<ProductPurchaseResponse>> response = productController.purchaseProducts(requests);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyElementsOf(serviceResponse);
    }

    @Test
    void shouldFindProductById() {
        UUID productId = UUID.randomUUID();
        ProductResponse serviceResponse = new ProductResponse(productId, "Laptop", "Gaming", 2, BigDecimal.TEN, UUID.randomUUID(), "Electronics", "Devices");
        when(productService.findById(productId)).thenReturn(serviceResponse);

        ResponseEntity<ProductResponse> response = productController.findById(productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void shouldFindAllProducts() {
        List<ProductResponse> serviceResponse = List.of(
                new ProductResponse(UUID.randomUUID(), "Laptop", "Gaming", 2, BigDecimal.TEN, UUID.randomUUID(), "Electronics", "Devices")
        );
        when(productService.findAll()).thenReturn(serviceResponse);

        ResponseEntity<List<ProductResponse>> response = productController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyElementsOf(serviceResponse);
    }
}
