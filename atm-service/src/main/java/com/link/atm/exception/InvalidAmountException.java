package com.link.atm.exception;

public class InvalidAmountException extends AtmException {
    public InvalidAmountException(String detail) {
        super("Importe invalido: " + detail, 400);
    }
}
