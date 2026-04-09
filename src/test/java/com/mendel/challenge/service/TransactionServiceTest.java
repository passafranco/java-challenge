package com.mendel.challenge.service;

import com.mendel.challenge.model.Transaction;
import com.mendel.challenge.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        transactionService.createTransaction(10L, 5000.0, "cars", null);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldCreateTransactionWithParentId() {
        transactionService.createTransaction(11L, 10000.0, "shopping", 10L);

        verify(transactionRepository).save(argThat(tx ->
                tx.getId().equals(11L) &&
                tx.getAmount().equals(10000.0) &&
                tx.getType().equals("shopping") &&
                tx.getParentId().equals(10L)
        ));
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
                .thenReturn(Optional.of(new Transaction(10L, 5000.0, "cars", null)));
        when(transactionRepository.findChildrenIds(10L))
                .thenReturn(Collections.emptyList());

        double sum = transactionService.getTransitiveSum(10L);

        assertEquals(5000.0, sum);
    }

    @Test
    void shouldGetTransitiveSumWithChildren() {
        // tx10 (5000) -> tx11 (10000) -> tx12 (5000)
        when(transactionRepository.findById(10L))
                .thenReturn(Optional.of(new Transaction(10L, 5000.0, "cars", null)));
        when(transactionRepository.findById(11L))
                .thenReturn(Optional.of(new Transaction(11L, 10000.0, "shopping", 10L)));
        when(transactionRepository.findById(12L))
                .thenReturn(Optional.of(new Transaction(12L, 5000.0, "shopping", 11L)));

        when(transactionRepository.findChildrenIds(10L)).thenReturn(List.of(11L));
        when(transactionRepository.findChildrenIds(11L)).thenReturn(List.of(12L));
        when(transactionRepository.findChildrenIds(12L)).thenReturn(Collections.emptyList());

        // sum(10) = 5000 + 10000 + 5000 = 20000
        assertEquals(20000.0, transactionService.getTransitiveSum(10L));

        // sum(11) = 10000 + 5000 = 15000
        assertEquals(15000.0, transactionService.getTransitiveSum(11L));
    }

    @Test
    void shouldReturnZeroForNonExistentTransaction() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        double sum = transactionService.getTransitiveSum(999L);

        assertEquals(0.0, sum);
    }
}
