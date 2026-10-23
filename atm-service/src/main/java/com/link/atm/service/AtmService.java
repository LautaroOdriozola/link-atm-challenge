package com.link.atm.service;

import java.math.BigDecimal;

public interface AtmService {

    void login(String cardNumber);

    BigDecimal withdraw(String cardNumber, String accountNumber, BigDecimal amount);

    BigDecimal deposit(String cardNumber, String cbu, BigDecimal amount);

    BigDecimal getBalance(String cardNumber, String accountNumber);
}
