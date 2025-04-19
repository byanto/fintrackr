package com.fintrackr.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fintrackr.model.Product;
import com.fintrackr.model.Transaction;
import com.fintrackr.model.TransactionType;
import com.fintrackr.repository.ProductRepository;
import com.fintrackr.repository.TransactionRepository;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /* 
     * Test the successful creation of an IN transaction.
     */
    @Test
    void testCreateTransactionInSuccess() {
        Product product = Product.builder().id(1L).name("Test Product").stock(5).build();
        Transaction transaction = Transaction.builder().id(1L).type(TransactionType.IN).quantity(3).product(product).build();

        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction newTransaction = transactionService.createTransaction(transaction);        
        assertEquals(8, newTransaction.getProduct().getStock());

        verify(productRepository, times(1)).save(product);
        verify(transactionRepository, times(1)).save(transaction);
    }

    /*
     * Test the successful creation of an OUT transaction.
     */
    @Test
    void testCreateTransactionOutSuccess() {
        Product product = Product.builder().id(1L).name("Test Product").stock(5).build();
        Transaction transaction = Transaction.builder().id(1L).type(TransactionType.OUT).quantity(3).product(product).build();

        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction newTransaction = transactionService.createTransaction(transaction);        
        assertEquals(2, newTransaction.getProduct().getStock());

        verify(productRepository, times(1)).save(product);
        verify(transactionRepository, times(1)).save(transaction);
    }

    /*
     * Test for OUT transaction with insufficient stock
     */
    @Test
    void testCreateTransactionWithInsufficientStock() {
        // Create mock product and transaction
        Product product = Product.builder().id(1L).name("Test Product").stock(5).build();
        Transaction transaction = Transaction.builder().id(1L).type(TransactionType.OUT).quantity(10).product(product).build();        

        // Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(transaction));
        System.out.println(ex.getMessage());
        assertEquals("Insufficient stock for the transaction.", ex.getMessage());

        verify(productRepository, never()).save(any(Product.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    /*
     * Test for invalid transaction type
     */
    @Test
    void testCreateTransactionWithInvalidType() {
        // Arrange
        Product product = Product.builder().id(1L).name("Test Product").stock(5).build();
        Transaction transaction = Transaction.builder().id(1L).quantity(3).product(product).type(null).build();        

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(transaction));
        assertEquals("Invalid transaction type.", ex.getMessage());

        // Verify no interactions with database
        verify(productRepository, never()).save(any(Product.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

}
