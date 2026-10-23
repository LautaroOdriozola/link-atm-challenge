package com.link.atm.exception;

public class InactiveCardException extends AtmException {
    public InactiveCardException(String cardNumber) {
        super("La tarjeta se encuentra inactiva", 403);
    }
}
