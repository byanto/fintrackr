package com.fintrackr.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void testCreateProduct() {
        Product inputProduct = Product.builder()
                                .name("Test Product")
                                .stock(5)
                                .build();

        when(productRepository.save(any(Product.class))).thenReturn(inputProduct);

        Product resultProduct = productService.createProduct(inputProduct);

        assertEquals("Test Product", resultProduct.getName());
        assertEquals(5, resultProduct.getStock());

        verify(productRepository, times(1)).save(inputProduct);
        
    }

}
