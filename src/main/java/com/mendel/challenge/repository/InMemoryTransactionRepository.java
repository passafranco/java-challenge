package com.mendel.challenge.repository;

import com.mendel.challenge.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> store = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> typeIndex = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> childrenIndex = new ConcurrentHashMap<>();

    @Override
    public void save(Transaction transaction) {
        store.put(transaction.getId(), transaction);

        typeIndex.computeIfAbsent(transaction.getType(), k -> new ArrayList<>())
                .add(transaction.getId());

        if (transaction.getParentId() != null) {
            childrenIndex.computeIfAbsent(transaction.getParentId(), k -> new ArrayList<>())
                    .add(transaction.getId());
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Long> findIdsByType(String type) {
        return typeIndex.getOrDefault(type, Collections.emptyList());
    }

    @Override
    public List<Long> findChildrenIds(Long parentId) {
        return childrenIndex.getOrDefault(parentId, Collections.emptyList());
    }
}
