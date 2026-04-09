package com.mendel.challenge.controller;

import com.mendel.challenge.dto.StatusResponse;
import com.mendel.challenge.dto.SumResponse;
import com.mendel.challenge.dto.TransactionRequest;
import com.mendel.challenge.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PutMapping("/{transaction_id}")
    public ResponseEntity<StatusResponse> createTransaction(
            @PathVariable("transaction_id") Long transactionId,
            @RequestBody TransactionRequest request) {
        transactionService.createTransaction(transactionId, request.getAmount(), request.getType(), request.getParentId());
        return ResponseEntity.ok(new StatusResponse("ok"));
    }

    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.getTransactionIdsByType(type));
    }

    @GetMapping("/sum/{transaction_id}")
    public ResponseEntity<SumResponse> getSum(@PathVariable("transaction_id") Long transactionId) {
        return ResponseEntity.ok(new SumResponse(transactionService.getTransitiveSum(transactionId)));
    }
}
