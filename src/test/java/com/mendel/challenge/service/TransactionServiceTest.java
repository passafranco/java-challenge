package com.mendel.challenge.service;

import com.mendel.challenge.exception.TransactionAlreadyExistsException;
import com.mendel.challenge.exception.TransactionNotFoundException;
import com.mendel.challenge.model.Transaction;
import com.mendel.challenge.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldCreateTransaction() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.empty());

        transactionService.createTransaction(10L, new BigDecimal("5000"), "cars", null);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldCreateTransactionWithParentId() {
        when(transactionRepository.findById(11L)).thenReturn(Optional.empty());
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(new Transaction(10L, new BigDecimal("5000"), "cars", null)));

        transactionService.createTransaction(11L, new BigDecimal("10000"), "shopping", 10L);

        verify(transactionRepository).save(argThat(tx ->
                tx.getId().equals(11L) &&
                tx.getAmount().compareTo(new BigDecimal("10000")) == 0 &&
                tx.getType().equals("shopping") &&
                tx.getParentId().equals(10L)
        ));
    }

    @Test
    void shouldThrowWhenTransactionAlreadyExists() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(new Transaction(10L, new BigDecimal("5000"), "cars", null)));

        assertThrows(TransactionAlreadyExistsException.class,
                () -> transactionService.createTransaction(10L, new BigDecimal("5000"), "cars", null));
    }

    @Test
    void shouldThrowWhenParentIdNotFound() {
        when(transactionRepository.findById(11L)).thenReturn(Optional.empty());
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.createTransaction(11L, new BigDecimal("5000"), "cars", 99L));
    }

    @Test
    void shouldGetTransactionIdsByType() {
        when(transactionRepository.findIdsByType("cars")).thenReturn(List.of(10L, 11L));

        List<Long> ids = transactionService.getTransactionIdsByType("cars");

        assertEquals(List.of(10L, 11L), ids);
    }

    @Test
    void shouldReturnEmptyListForUnknownType() {
        when(transactionRepository.findIdsByType("unknown")).thenReturn(Collections.emptyList());

        List<Long> ids = transactionService.getTransactionIdsByType("unknown");

        assertTrue(ids.isEmpty());
    }

    @Test
    void shouldGetTransitiveSumForSingleTransaction() {
        when(transactionRepository.findById(10L))
                .thenReturn(Optional.of(new Transaction(10L, new BigDecimal("5000"), "cars", null)));
        when(transactionRepository.findChildrenIds(10L))
                .thenReturn(Collections.emptyList());

        BigDecimal sum = transactionService.getTransitiveSum(10L);

        assertEquals(0, new BigDecimal("5000").compareTo(sum));
    }

    @Test
    void shouldGetTransitiveSumWithChildren() {
        // tx10 (5000) -> tx11 (10000) -> tx12 (5000)
        when(transactionRepository.findById(10L))
                .thenReturn(Optional.of(new Transaction(10L, new BigDecimal("5000"), "cars", null)));
        when(transactionRepository.findById(11L))
                .thenReturn(Optional.of(new Transaction(11L, new BigDecimal("10000"), "shopping", 10L)));
        when(transactionRepository.findById(12L))
                .thenReturn(Optional.of(new Transaction(12L, new BigDecimal("5000"), "shopping", 11L)));

        when(transactionRepository.findChildrenIds(10L)).thenReturn(List.of(11L));
        when(transactionRepository.findChildrenIds(11L)).thenReturn(List.of(12L));
        when(transactionRepository.findChildrenIds(12L)).thenReturn(Collections.emptyList());

        // sum(10) = 5000 + 10000 + 5000 = 20000
        assertEquals(0, new BigDecimal("20000").compareTo(transactionService.getTransitiveSum(10L)));

        // sum(11) = 10000 + 5000 = 15000
        assertEquals(0, new BigDecimal("15000").compareTo(transactionService.getTransitiveSum(11L)));
    }

    @Test
    void shouldThrowWhenTransitiveSumForNonExistentTransaction() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransitiveSum(999L));
    }

    @Test
    void shouldAcceptAmountWithTwoDecimalPlaces() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.empty());

        transactionService.createTransaction(10L, new BigDecimal("5000.50"), "cars", null);

        verify(transactionRepository).save(argThat(tx ->
                tx.getAmount().compareTo(new BigDecimal("5000.50")) == 0
        ));
    }

    @Test
    void shouldAcceptAmountWithOneDecimalPlace() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.empty());

        transactionService.createTransaction(10L, new BigDecimal("5000.5"), "cars", null);

        verify(transactionRepository).save(argThat(tx ->
                tx.getAmount().scale() == 2
        ));
    }

    @Test
    void shouldAcceptAmountWithNoDecimals() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.empty());

        transactionService.createTransaction(10L, new BigDecimal("5000"), "cars", null);

        verify(transactionRepository).save(argThat(tx ->
                tx.getAmount().scale() == 2
        ));
    }

    @Test
    void shouldReturnSumWithTwoDecimalPlaces() {
        when(transactionRepository.findById(10L))
                .thenReturn(Optional.of(new Transaction(10L, new BigDecimal("5000.50"), "cars", null)));
        when(transactionRepository.findChildrenIds(10L))
                .thenReturn(Collections.emptyList());

        BigDecimal sum = transactionService.getTransitiveSum(10L);

        assertEquals(2, sum.scale());
        assertEquals(0, new BigDecimal("5000.50").compareTo(sum));
    }
}
