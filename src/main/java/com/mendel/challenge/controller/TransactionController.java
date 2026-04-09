package com.mendel.challenge.controller;

import com.mendel.challenge.dto.StatusResponse;
import com.mendel.challenge.dto.SumResponse;
import com.mendel.challenge.dto.TransactionRequest;
import com.mendel.challenge.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints de gestion de transacciones")
public class TransactionController {

    private final TransactionService transactionService;

    @PutMapping("/{transaction_id}")
    @Operation(summary = "Crear una transaccion", description = "Almacena una nueva transaccion con un monto, tipo y un vinculo opcional a una transaccion padre mediante parent_id")
    @ApiResponse(responseCode = "200", description = "Transaccion creada exitosamente")
    public ResponseEntity<StatusResponse> createTransaction(
            @PathVariable("transaction_id") Long transactionId,
            @Valid @RequestBody TransactionRequest request) {
        transactionService.createTransaction(transactionId, request.getAmount(), request.getType(), request.getParentId());
        return ResponseEntity.ok(new StatusResponse("ok"));
    }

    @GetMapping("/types/{type}")
    @Operation(summary = "Obtener IDs de transacciones por tipo", description = "Devuelve una lista con los IDs de todas las transacciones que coinciden con el tipo especificado")
    @ApiResponse(responseCode = "200", description = "Lista de IDs de transacciones")
    public ResponseEntity<List<Long>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.getTransactionIdsByType(type));
    }

    @GetMapping("/sum/{transaction_id}")
    @Operation(summary = "Obtener suma transitiva", description = "Devuelve la suma del monto de la transaccion y todos sus hijos vinculados transitivamente por parent_id")
    @ApiResponse(responseCode = "200", description = "Suma de las transacciones vinculadas")
    public ResponseEntity<SumResponse> getSum(@PathVariable("transaction_id") Long transactionId) {
        return ResponseEntity.ok(new SumResponse(transactionService.getTransitiveSum(transactionId)));
    }
}
