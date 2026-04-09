package com.mendel.challenge.controller;

import com.mendel.challenge.dto.StatusResponse;
import com.mendel.challenge.dto.SumResponse;
import com.mendel.challenge.dto.TransactionRequest;
import com.mendel.challenge.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @PutMapping("/{transaction_id}")
    @Operation(summary = "Create a transaction", description = "Stores a new transaction with an optional parent link")
    @ApiResponse(responseCode = "200", description = "Transaction created successfully")
    public ResponseEntity<StatusResponse> createTransaction(
            @PathVariable("transaction_id") Long transactionId,
            @RequestBody TransactionRequest request) {
        transactionService.createTransaction(transactionId, request.getAmount(), request.getType(), request.getParentId());
        return ResponseEntity.ok(new StatusResponse("ok"));
    }

    @GetMapping("/types/{type}")
    @Operation(summary = "Get transaction IDs by type", description = "Returns all transaction IDs matching the given type")
    @ApiResponse(responseCode = "200", description = "List of transaction IDs")
    public ResponseEntity<List<Long>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.getTransactionIdsByType(type));
    }

    @GetMapping("/sum/{transaction_id}")
    @Operation(summary = "Get transitive sum", description = "Returns the sum of the transaction and all its transitively linked children")
    @ApiResponse(responseCode = "200", description = "Sum of linked transactions")
    public ResponseEntity<SumResponse> getSum(@PathVariable("transaction_id") Long transactionId) {
        return ResponseEntity.ok(new SumResponse(transactionService.getTransitiveSum(transactionId)));
    }
}
