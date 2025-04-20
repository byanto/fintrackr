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

    @Test
    void testGetAllProducts() {
        // Arrange
        Product product1 = Product.builder()
                                .name("Test Product 1")
                                .stock(5)
                                .build();
        Product product2 = Product.builder()
                                .name("Test Product 2")
                                .stock(10)
                                .build();
        
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);

        // Assert
        List<Product> productList = productService.getAllProducts();
        assertEquals(2, productList.size());
        assertEquals("Test Product 1", productList.get(0).getName());
        assertEquals("Test Product 2", productList.get(1).getName());
        assertEquals(10, productList.get(1).getStock());
        assertEquals(5, productList.get(0).getStock());
        assertNotNull(productList);
        
        // Verify interactions
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById() {
        // Arrange
        Product inputProduct = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .stock(5)
                                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(inputProduct));

        // Assert that the product returned matches the input product
        Product result = productService.getProductById(1L).get();
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(5, result.getStock());

        // Verify that the repository's findById method was called once
        verify(productRepository, times(1)).findById(1L);
    }
    
    @Test
    void testCreateProduct() {
        // Arrange
        Product inputProduct = Product.builder()
                                .name("Test Product")
                                .stock(5)
                                .build();

        when(productRepository.save(any(Product.class))).thenReturn(inputProduct);

        // Act
        Product resultProduct = productService.createProduct(inputProduct);

        // Assert
        assertEquals("Test Product", resultProduct.getName());
        assertEquals(5, resultProduct.getStock());

        // Verify that the save method was called exactly once with the input product
        verify(productRepository, times(1)).save(inputProduct);
    }

    @Test
    void testUpdateProduct_WhenProductExists_ShouldRemoveFromRepository() {
        // Arrange
        Product inputProduct = Product.builder()
                                .id(1L)     
                                .name("Test Product")
                                .stock(5)
                                .build();
        
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(inputProduct));
        when(productRepository.save(any(Product.class))).thenReturn(inputProduct);

        // Assert that the product was updated correctly
        Product updatedProduct = productService.updateProduct(1L, inputProduct);
        assertNotNull(updatedProduct);
        assertEquals("Test Product", updatedProduct.getName());
        assertEquals(5, updatedProduct.getStock());

        // Verify that the repository's save method was called once
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

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
        IllegalArgumentException ex  = assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(inputProduct.getId(), inputProduct));
        assertEquals("Product with id " + inputProduct.getId() + " does not exist.", ex.getMessage());
    
        // Verify
        verify(productRepository, never()).save(any(Product.class));

    }

    @Test
    void testDeleteProduct_WhenProductExists_ShouldRemoveFromRepository() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(true);

        // Act
        productService.deleteProduct(1L);

        // Verify
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        Long id = 1L;
        when(productRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException ex  = assertThrows(IllegalArgumentException.class, () -> productService.deleteProduct(id));
        assertEquals("Product with id " + id + " does not exist.", ex.getMessage());

        // Verify
        verify(productRepository, never()).deleteById(anyLong());
    }

    

}
