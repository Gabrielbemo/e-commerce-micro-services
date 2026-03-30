package com.gabriel.ecommerce.product;

import com.gabriel.ecommerce.category.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper productMapper = new ProductMapper();

    @Test
    void shouldMapRequestToProduct() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        ProductRequest request = new ProductRequest(
                productId,
                "Laptop",
                "Gaming laptop",
                5,
                BigDecimal.valueOf(12.5),
                categoryId
        );

        Product product = productMapper.toProduct(request);

        assertThat(product.getId()).isEqualTo(productId);
        assertThat(product.getName()).isEqualTo("Laptop");
        assertThat(product.getCategory().getId()).isEqualTo(categoryId);
    }

    @Test
    void shouldMapProductToResponse() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Electronics").description("Devices").build();
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .description("Gaming laptop")
                .availableQuantity(10)
                .price(BigDecimal.TEN)
                .category(category)
                .build();

        ProductResponse response = productMapper.toProductResponse(product);

        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.categoryId()).isEqualTo(categoryId);
        assertThat(response.categoryName()).isEqualTo("Electronics");
    }

    @Test
    void shouldMapProductToPurchaseResponse() {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .description("Gaming laptop")
                .availableQuantity(2)
                .price(BigDecimal.TEN)
                .build();

        ProductPurchaseResponse response = productMapper.toProductPurchaseResponse(product);

        assertThat(response.productId()).isEqualTo(product.getId());
        assertThat(response.quantity()).isEqualTo(2);
    }
}
