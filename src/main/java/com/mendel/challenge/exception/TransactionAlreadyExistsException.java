package com.mendel.challenge.exception;

public class TransactionAlreadyExistsException extends RuntimeException {

    public TransactionAlreadyExistsException(Long id) {
        super("Transaction already exists with id: " + id);
    }
}
