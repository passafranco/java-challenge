package com.mendel.challenge.service;

import com.mendel.challenge.model.Transaction;
import com.mendel.challenge.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public void createTransaction(Long id, Double amount, String type, Long parentId) {
        Transaction transaction = new Transaction(id, amount, type, parentId);
        transactionRepository.save(transaction);
    }

    public List<Long> getTransactionIdsByType(String type) {
        return transactionRepository.findIdsByType(type);
    }

    public double getTransitiveSum(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .map(tx -> tx.getAmount() + sumChildren(transactionId))
                .orElse(0.0);
    }

    private double sumChildren(Long parentId) {
        return transactionRepository.findChildrenIds(parentId).stream()
                .flatMap(childId -> transactionRepository.findById(childId).stream())
                .mapToDouble(child -> child.getAmount() + sumChildren(child.getId()))
                .sum();
    }
}
