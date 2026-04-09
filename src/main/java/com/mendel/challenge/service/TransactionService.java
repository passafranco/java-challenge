package com.mendel.challenge.service;

import com.mendel.challenge.exception.TransactionAlreadyExistsException;
import com.mendel.challenge.exception.TransactionNotFoundException;
import com.mendel.challenge.model.Transaction;
import com.mendel.challenge.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int SCALE = 2;

    private final TransactionRepository transactionRepository;

    public void createTransaction(Long id, BigDecimal amount, String type, Long parentId) {
        if (transactionRepository.findById(id).isPresent()) {
            throw new TransactionAlreadyExistsException(id);
        }

        if (parentId != null && transactionRepository.findById(parentId).isEmpty()) {
            throw new TransactionNotFoundException(parentId);
        }

        Transaction transaction = new Transaction(id, amount.setScale(SCALE, RoundingMode.HALF_UP), type, parentId);
        transactionRepository.save(transaction);
    }

    public List<Long> getTransactionIdsByType(String type) {
        return transactionRepository.findIdsByType(type);
    }

    public BigDecimal getTransitiveSum(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .map(tx -> tx.getAmount().add(sumChildren(transactionId))
                        .setScale(SCALE, RoundingMode.HALF_UP))
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    private BigDecimal sumChildren(Long parentId) {
        return transactionRepository.findChildrenIds(parentId).stream()
                .flatMap(childId -> transactionRepository.findById(childId).stream())
                .map(child -> child.getAmount().add(sumChildren(child.getId())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
