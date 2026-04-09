package com.mendel.challenge.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private Long id;
    private BigDecimal amount;
    private String type;
    private Long parentId;
}
