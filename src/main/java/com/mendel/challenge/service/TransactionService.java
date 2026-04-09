package com.mendel.challenge.service;

import com.mendel.challenge.exception.InvalidTransactionException;
import com.mendel.challenge.exception.TransactionAlreadyExistsException;
import com.mendel.challenge.exception.TransactionNotFoundException;
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
        if (amount == null || type == null || type.isBlank()) {
            throw new InvalidTransactionException("Amount and type are required");
        }

        if (transactionRepository.findById(id).isPresent()) {
            throw new TransactionAlreadyExistsException(id);
        }

        if (parentId != null && transactionRepository.findById(parentId).isEmpty()) {
            throw new TransactionNotFoundException(parentId);
        }

        Transaction transaction = new Transaction(id, amount, type, parentId);
        transactionRepository.save(transaction);
    }

    public List<Long> getTransactionIdsByType(String type) {
        return transactionRepository.findIdsByType(type);
    }

    public double getTransitiveSum(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .map(tx -> tx.getAmount() + sumChildren(transactionId))
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    private double sumChildren(Long parentId) {
        return transactionRepository.findChildrenIds(parentId).stream()
                .flatMap(childId -> transactionRepository.findById(childId).stream())
                .mapToDouble(child -> child.getAmount() + sumChildren(child.getId()))
                .sum();
    }
}
