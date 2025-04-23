package com.fintrackr.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintrackr.model.Product;
import com.fintrackr.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service class responsible for managing product-related business operations.
 * Handles product creation, updates, retrieval, and stock management functionality.
 * 
 * <p>This service provides an abstraction layer between the controller and repository,
 * ensuring proper validation and business logic implementation for product operations.</p>
 *
 * @author Budi Yanto
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Retrieves all products.
     *
     * @return a list of all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id the unique identifier of the product
     * @return the found product entity
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product with id " + id + " does not exist."));
    }

    /**
     * Creates a new product.
     *
     * @param product the product entity to be created
     * @return the saved product with generated ID
     */
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * Updates an existing product's information.
     *
     * @param id the unique identifier of the product to update
     * @param updatedProduct the updated product information
     * @return the updated product entity
     * @throws IllegalArgumentException if no product exists with the given ID
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setStock(updatedProduct.getStock());
            return productRepository.save(product);
        }).orElseThrow(() -> new NoSuchElementException("Product with id " + id + " does not exist."));
    }

    /**
     * Deletes a product.
     *
     * @param id the unique identifier of the product to delete
     * @throws IllegalArgumentException if no product exists with the given ID
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("Product with id " + id + " does not exist.");
        }
        productRepository.deleteById(id);
    }

}
