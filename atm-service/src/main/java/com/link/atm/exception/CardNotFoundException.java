package com.link.atm.exception;

public class CardNotFoundException extends AtmException {
    public CardNotFoundException(String cardNumber) {
        super("La tarjeta no existe", 404);
    }
}
