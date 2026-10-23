package com.link.atm.exception;

public class AccountNotLinkedException extends AtmException {
    public AccountNotLinkedException(String cardNumber, String accountNumber) {
        super("La cuenta no pertenece a la tarjeta indicada", 403);
    }
}
