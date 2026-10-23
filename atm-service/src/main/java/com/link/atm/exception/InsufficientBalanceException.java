package com.link.atm.exception;

public class InsufficientBalanceException extends AtmException {
    public InsufficientBalanceException() {
        super("Saldo insuficiente para realizar la extraccion", 409);
    }
}
