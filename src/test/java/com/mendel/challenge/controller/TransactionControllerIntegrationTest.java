package com.mendel.challenge.controller;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void shouldCreateTransaction10() throws Exception {
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 5000, \"type\": \"cars\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    @Order(2)
    void shouldCreateTransaction11WithParent10() throws Exception {
        mockMvc.perform(put("/transactions/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 10000, \"type\": \"shopping\", \"parent_id\": 10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    @Order(3)
    void shouldCreateTransaction12WithParent11() throws Exception {
        mockMvc.perform(put("/transactions/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 5000, \"type\": \"shopping\", \"parent_id\": 11}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    @Order(4)
    void shouldGetTransactionIdsByTypeCars() throws Exception {
        mockMvc.perform(get("/transactions/types/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(10));
    }

    @Test
    @Order(5)
    void shouldGetTransitiveSumForTransaction10() throws Exception {
        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(20000.0));
    }

    @Test
    @Order(6)
    void shouldGetTransitiveSumForTransaction11() throws Exception {
        mockMvc.perform(get("/transactions/sum/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(15000.0));
    }

    @Test
    @Order(7)
    void shouldReturnConflictForDuplicateTransaction() throws Exception {
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 9999, \"type\": \"cars\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Transaction already exists with id: 10"));
    }

    @Test
    @Order(8)
    void shouldReturnNotFoundForNonExistentParent() throws Exception {
        mockMvc.perform(put("/transactions/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1000, \"type\": \"food\", \"parent_id\": 999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: 999"));
    }

    @Test
    @Order(9)
    void shouldReturnNotFoundForSumOfNonExistentTransaction() throws Exception {
        mockMvc.perform(get("/transactions/sum/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: 999"));
    }

    @Test
    @Order(10)
    void shouldReturnEmptyListForUnknownType() throws Exception {
        mockMvc.perform(get("/transactions/types/unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Order(11)
    void shouldReturnBadRequestForMissingFields() throws Exception {
        mockMvc.perform(put("/transactions/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1000}"))
                .andExpect(status().isBadRequest());
    }
}
