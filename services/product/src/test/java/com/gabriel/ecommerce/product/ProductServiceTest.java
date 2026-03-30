package com.gabriel.ecommerce.product;

import com.gabriel.ecommerce.category.Category;
import com.gabriel.ecommerce.exception.ProductPurchaseException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        ProductRequest request = new ProductRequest(null, "Laptop", "Gaming", 10, BigDecimal.TEN, UUID.randomUUID());
        Product mapped = Product.builder().name("Laptop").build();
        UUID id = UUID.randomUUID();
        Product saved = Product.builder().id(id).build();

        when(productMapper.toProduct(request)).thenReturn(mapped);
        when(productRepository.save(mapped)).thenReturn(saved);

        UUID result = productService.createProduct(request);

        assertThat(result).isEqualTo(id);
    }

    @Test
    void shouldThrowWhenSomeProductsAreMissing() {
        UUID productId = UUID.randomUUID();
        List<ProductPurchaseRequest> requests = List.of(new ProductPurchaseRequest(productId, 1));
        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of());

        assertThatThrownBy(() -> productService.purchaseProducts(requests))
                .isInstanceOf(ProductPurchaseException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldThrowWhenQuantityIsInsufficient() {
        UUID productId = UUID.randomUUID();
        List<ProductPurchaseRequest> requests = List.of(new ProductPurchaseRequest(productId, 10));
        Product stored = Product.builder()
                .id(productId)
                .name("Laptop")
                .availableQuantity(2)
                .price(BigDecimal.TEN)
                .category(Category.builder().id(UUID.randomUUID()).build())
                .build();

        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of(stored));

        assertThatThrownBy(() -> productService.purchaseProducts(requests))
                .isInstanceOf(ProductPurchaseException.class)
                .hasMessageContaining("Not enough quantity");
    }

    @Test
    void shouldPurchaseProductsSuccessfully() {
        UUID productId = UUID.randomUUID();
        List<ProductPurchaseRequest> requests = List.of(new ProductPurchaseRequest(productId, 3));
        Product stored = Product.builder()
                .id(productId)
                .name("Laptop")
                .description("Gaming")
                .availableQuantity(10)
                .price(BigDecimal.TEN)
                .category(Category.builder().id(UUID.randomUUID()).build())
                .build();
        ProductPurchaseResponse purchaseResponse = new ProductPurchaseResponse(productId, "Laptop", "Gaming", BigDecimal.TEN, 7);

        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of(stored));
        when(productMapper.toProductPurchaseResponse(stored)).thenReturn(purchaseResponse);

        List<ProductPurchaseResponse> result = productService.purchaseProducts(requests);

        assertThat(result).containsExactly(purchaseResponse);
        assertThat(stored.getAvailableQuantity()).isEqualTo(7);
        verify(productMapper).toProductPurchaseResponse(stored);
    }

    @Test
    void shouldFindById() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().id(productId).build();
        ProductResponse response = new ProductResponse(productId, "Laptop", "Gaming", 1, BigDecimal.TEN, UUID.randomUUID(), "Electronics", "Devices");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(product)).thenReturn(response);

        ProductResponse result = productService.findById(productId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenProductNotFoundById() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldFindAllProducts() {
        Product product = Product.builder().id(UUID.randomUUID()).build();
        ProductResponse response = new ProductResponse(product.getId(), "Laptop", "Gaming", 1, BigDecimal.TEN, UUID.randomUUID(), "Electronics", "Devices");

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toProductResponse(product)).thenReturn(response);

        List<ProductResponse> result = productService.findAll();

        assertThat(result).containsExactly(response);
    }
}
