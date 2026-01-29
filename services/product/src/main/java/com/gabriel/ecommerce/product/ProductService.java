package com.gabriel.ecommerce.product;

import com.gabriel.ecommerce.exception.ProductPurchaseException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public UUID createProduct(@Valid ProductRequest request) {
        var product = productMapper.toProduct(request);
        return productRepository.save(product).getId();
    }

    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> productPurchaseRequestList) {
        var productIds = productPurchaseRequestList.stream()
                .map(ProductPurchaseRequest::productId)
                .toList();

        var storedProducts = productRepository.findAllById(productIds);
        if(productIds.size() != storedProducts.size()){
            throw new ProductPurchaseException("One or more products not found");
        }

        Map<UUID, ProductPurchaseRequest> productsPurchasedHashMap = productPurchaseRequestList
                .stream()
                .collect(Collectors.toMap(
                        ProductPurchaseRequest::productId,
                        Function.identity()
                ));

        var purchasedProducts = new ArrayList<ProductPurchaseResponse>();

        for (var storedProduct : storedProducts) {
            var newQuantity = storedProduct.getAvailableQuantity() - productsPurchasedHashMap.get(storedProduct.getId()).quantity();
            if(newQuantity < 0){
                throw new ProductPurchaseException("Not enough quantity of product: " + storedProduct.getName());
            }
            storedProduct.setAvailableQuantity(newQuantity);
            purchasedProducts.add(productMapper.toProductPurchaseResponse(storedProduct));
        }

        return purchasedProducts;
    }

    public ProductResponse findById(UUID productId) {
        return productRepository.findById(productId)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toProductResponse)
                .toList();
    }
}
