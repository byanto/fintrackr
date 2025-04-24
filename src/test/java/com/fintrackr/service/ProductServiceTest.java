package com.fintrackr.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fintrackr.model.Product;
import com.fintrackr.repository.ProductRepository;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the retrieval of all products.
     * Verifies that:
     * - The list of products returned matches the expected list
     * - The size of the list is correct
     * - The names, stock, and quantity of the retrieved products are correct
     */
    @Test
    void testGetAllProducts_ShouldReturnListOfProducts() {
        // Arrange
        Product product1 = Product.builder()
                .name("Test Product 1")
                .stock(5)
                .build();
        Product product2 = Product.builder()
                .name("Test Product 2")
                .stock(10)
                .build();

        List<Product> expectedProducts = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(expectedProducts, result);
        assertEquals(2, result.size());
        assertEquals("Test Product 1", result.get(0).getName());
        assertEquals("Test Product 2", result.get(1).getName());
        assertEquals(10, result.get(1).getStock());
        assertEquals(5, result.get(0).getStock());
        verify(productRepository, times(1)).findAll();
    }

    /**
     * Tests the retrieval of a product by its unique identifier, when the product
     * exists.
     * Verifies that:
     * - The retrieved product matches the expected product
     * - The repository's findById method was called once
     */
    @Test
    void testGetProductById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        Product inputProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .stock(5)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(inputProduct));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals(5, result.getStock());
        verify(productRepository, times(1)).findById(1L);
    }

    /**
     * Tests the retrieval of a product by its unique identifier, when the product
     * does not exist.
     * Verifies that:
     * - NoSuchElementException is thrown with the correct message
     * - The repository's findById method was called once
     */
    @Test
    void testGetProductById_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> productService.getProductById(productId));
        assertEquals("Product with id " + productId + " does not exist.", ex.getMessage());
        verify(productRepository, times(1)).findById(productId);
    }

    /**
     * Tests the creation of a product.
     * Verifies that:
     * - The returned product matches the expected product
     * - The repository's save method was called once with the input product
     */
    @Test
    void testCreateProduct_ShouldReturnProduct() {
        // Arrange
        Product inputProduct = Product.builder()
                .name("Test Product")
                .stock(5)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(inputProduct);

        // Act
        Product result = productService.createProduct(inputProduct);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(5, result.getStock());

        // Verify that the save method was called exactly once with the input product
        verify(productRepository, times(1)).save(inputProduct);
    }

    /**
     * Tests the update of an existing product.
     * Verifies that:
     * - The returned product matches the expected product
     * - The repository's findById method was called once with the input id
     * - The repository's save method was called once with the updated product
     */
    @Test
    void testUpdateProduct_WhenProductExists_ShouldRemoveFromRepository() {
        // Arrange
        Product inputProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .stock(5)
                .build();
        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Updated Product")
                .stock(10)
                .build();

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(inputProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        Product result = productService.updateProduct(1L, inputProduct);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(10, result.getStock());

        // Verify that the repository's save method was called once
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    /**
     * Tests the update of an existing product, when the product does not exist.
     * Verifies that:
     * - NoSuchElementException is thrown with the correct message
     * - The repository's save method was not called
     */
    @Test
    void testUpdateProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        Product inputProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .stock(5)
                .build();

        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> productService.updateProduct(inputProduct.getId(), inputProduct));
        assertEquals("Product with id " + inputProduct.getId() + " does not exist.", ex.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Tests the deletion of an existing product.
     * Verifies that:
     * - The repository's deleteById method was called once with the input id
     */
    @Test
    void testDeleteProduct_WhenProductExists_ShouldRemoveFromRepository() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(true);

        // Act
        productService.deleteProduct(1L);

        // Verify
        verify(productRepository, times(1)).deleteById(1L);
    }

    /**
     * Tests the deletion of a product when the product does not exist.
     * Verifies that:
     * - NoSuchElementException is thrown with the correct message
     * - The repository's deleteById method is not called
     */
    @Test
    void testDeleteProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        Long id = 1L;
        when(productRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> productService.deleteProduct(id));
        assertEquals("Product with id " + id + " does not exist.", ex.getMessage());

        // Verify
        verify(productRepository, never()).deleteById(anyLong());
    }

}
