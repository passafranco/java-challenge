package com.mendel.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SumResponse {

    private BigDecimal sum;
}
