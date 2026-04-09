package com.mendel.challenge.repository;

import com.mendel.challenge.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionRepositoryTest {

    private InMemoryTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
    }

    @Test
    void shouldSaveAndFindById() {
        Transaction tx = new Transaction(10L, new BigDecimal("5000"), "cars", null);
        repository.save(tx);

        Optional<Transaction> found = repository.findById(10L);

        assertTrue(found.isPresent());
        assertEquals(0, new BigDecimal("5000").compareTo(found.get().getAmount()));
        assertEquals("cars", found.get().getType());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<Transaction> found = repository.findById(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindIdsByType() {
        repository.save(new Transaction(10L, new BigDecimal("5000"), "cars", null));
        repository.save(new Transaction(11L, new BigDecimal("3000"), "cars", null));
        repository.save(new Transaction(12L, new BigDecimal("7000"), "shopping", null));

        List<Long> carIds = repository.findIdsByType("cars");

        assertEquals(2, carIds.size());
        assertTrue(carIds.containsAll(List.of(10L, 11L)));
    }

    @Test
    void shouldReturnEmptyListForUnknownType() {
        List<Long> ids = repository.findIdsByType("unknown");

        assertTrue(ids.isEmpty());
    }

    @Test
    void shouldFindChildrenIds() {
        repository.save(new Transaction(10L, new BigDecimal("5000"), "cars", null));
        repository.save(new Transaction(11L, new BigDecimal("10000"), "shopping", 10L));
        repository.save(new Transaction(12L, new BigDecimal("5000"), "shopping", 10L));

        List<Long> children = repository.findChildrenIds(10L);

        assertEquals(2, children.size());
        assertTrue(children.containsAll(List.of(11L, 12L)));
    }

    @Test
    void shouldReturnEmptyListWhenNoChildren() {
        repository.save(new Transaction(10L, new BigDecimal("5000"), "cars", null));

        List<Long> children = repository.findChildrenIds(10L);

        assertTrue(children.isEmpty());
    }
}
