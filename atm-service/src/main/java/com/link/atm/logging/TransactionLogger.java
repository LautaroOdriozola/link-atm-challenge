package com.link.atm.logging;

import java.math.BigDecimal;

public interface TransactionLogger {

    enum Result {
        OK, ERROR
    }

    void log(String operation, String cardNumber, String accountOrCbu, BigDecimal amount,
             Result result, String detail, BigDecimal resultingBalance);
}
