package com.link.atm.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequest {
    private String cardNumber;
    private String cbu;
    private BigDecimal amount;
}
