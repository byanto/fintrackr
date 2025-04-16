package com.fintrackr.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintrackr.model.Product;
import com.fintrackr.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setStock(updatedProduct.getStock());
            return productRepository.save(product);
        }).orElseThrow(() -> new IllegalArgumentException("Product with id " + id + " does not exist."));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product with id " + id + " does not exist.");
        }
        productRepository.deleteById(id);
    }

}
