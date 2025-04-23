package com.fintrackr.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

/**
 * Unit tests for {@link TransactionService} class focusing on transaction
 * creation
 * and stock management validation.
 */
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

    /**
     * Tests the retrieval of all transactions.
     * Verifies that:
     * - The list of transactions returned matches the expected list
     * - The size of the list is correct
     */
    @Test
    void testGetAllTransactions_ShouldReturnListOfTransactions() {
        // Arrange
        Product product = Product.builder().id(1L).name("Test Product").stock(10).build();
        Transaction transaction1 = Transaction.builder().id(1L).type(TransactionType.IN).quantity(5).product(product)
                .build();
        Transaction transaction2 = Transaction.builder().id(2L).type(TransactionType.OUT).quantity(2).product(product)
                .build();
        List<Transaction> expectedTransactions = List.of(transaction1, transaction2);
        when(transactionRepository.findAll()).thenReturn(expectedTransactions);

        // Act
        List<Transaction> result = transactionService.getAllTransactions();

        // Assert
        assertNotNull(result);
        assertEquals(expectedTransactions, result);
        assertEquals(expectedTransactions.size(), result.size());
        assertEquals(transaction1, result.get(0));
        assertEquals(transaction2, result.get(1));
        verify(transactionRepository, times(1)).findAll();
    }

    /**
     * Tests the retrieval of a transaction by its unique identifier.
     * Verifies that:
     * - The retrieved transaction matches the expected transaction
     * - The quantity, product name, and product stock of the retrieved transaction
     * are correct
     */
    @Test
    void testGetTransactionById_WhenTransactionExists_ShouldReturnTransaction() {
        // Arrange
        Product product = Product.builder().id(1L).name("Test Product").stock(10).build();
        Transaction transaction = Transaction.builder().id(1L).type(TransactionType.IN).quantity(5).product(product)
                .build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        // Act
        Transaction result = transactionService.getTransactionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Optional.of(transaction), result);
        assertEquals(1L, result.getId());
        assertEquals(5, result.getQuantity());
        assertEquals("Test Product", result.getProduct().getName());
        assertEquals(10, result.getProduct().getStock());
        verify(transactionRepository, times(1)).findById(1L);
    }

    /**
     * Tests the retrieval of a transaction by its unique identifier, when the
     * transaction is not found.
     * Verifies that:
     * - NoSuchElementException is thrown with the correct message
     * - The repository's findById method was called once
     */
    @Test
    void testGetTransactionById_WhenTransactionDoesNotExist_ShouldThrowException() {
        // Arrange
        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> transactionService.getTransactionById(transactionId));
        assertEquals("Transaction with id " + transactionId + " does not exist.", ex.getMessage());
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    /**
     * Tests the creation of an IN transaction with valid inputs.
     * Verifies that:
     * - Product stock is correctly increased
     * - Both product and transaction are saved
     * - Final stock calculation is accurate
     */
    @Test
    void testCreateTransaction_InSuccess_ShouldIncreaseStock() {
        // Arrange
        Product product = Product.builder().id(1L).name("Test Product").stock(5).build();
        Transaction transaction = Transaction.builder().id(1L).type(TransactionType.IN).quantity(3).product(product)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        Transaction result = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(1L, transaction.getId());
        assertEquals(TransactionType.IN, transaction.getType());
        assertEquals(3, transaction.getQuantity());
        assertEquals("Test Product", result.getProduct().getName());
        assertEquals(8, result.getProduct().getStock());
        verify(productRepository, times(1)).save(product);
        verify(transactionRepository, times(1)).save(transaction);
    }

    /**
     * Tests the creation of an OUT transaction with valid inputs.
     * Verifies that:
     * - Product stock is correctly decreased
     * - Both product and transaction are saved
     * - Final stock calculation is accurate
     */
    @Test
    void testCreateTransaction_OutSuccess_ShouldDecreaseStock() {
        // Arrange
        Product product = Product.builder().id(1L).name("Test Product").stock(5).build();
        Transaction transaction = Transaction.builder().id(1L).type(TransactionType.OUT).quantity(3).product(product)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        Transaction result = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(1L, transaction.getId());
        assertEquals(TransactionType.OUT, transaction.getType());
        assertEquals(3, transaction.getQuantity());
        assertEquals("Test Product", result.getProduct().getName());
        assertEquals(2, result.getProduct().getStock());
        verify(productRepository, times(1)).save(product);
        verify(transactionRepository, times(1)).save(transaction);
    }

    /**
     * Tests the validation of OUT transactions when requested quantity
     * exceeds available stock.
     * Verifies that:
     * - IllegalArgumentException is thrown with correct message
     * - No database operations are performed
     */
    @Test
    void testCreateTransaction_WhenInsufficientStock_ShouldThrowException() {
        // Arrange
        Product product = Product.builder().id(1L).name("Test Product").stock(5).build();
        Transaction transaction = Transaction.builder().id(1L).type(TransactionType.OUT).quantity(10).product(product)
                .build();

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(transaction));
        assertEquals("Insufficient stock for the transaction.", ex.getMessage());

        // Verify no interactions with database
        verify(productRepository, never()).save(any(Product.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    /**
     * Tests the validation of transactions with invalid/null transaction type.
     * Verifies that:
     * - IllegalArgumentException is thrown with correct message
     * - No database operations are performed
     */
    @Test
    void testCreateTransaction_WhenInvalidType_ShouldThrowException() {
        // Arrange
        Product product = Product.builder().id(1L).name("Test Product").stock(5).build();
        Transaction transaction = Transaction.builder().id(1L).quantity(3).product(product).type(null).build();

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(transaction));
        assertEquals("Invalid transaction type.", ex.getMessage());

        // Verify no interactions with database
        verify(productRepository, never()).save(any(Product.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    

}
