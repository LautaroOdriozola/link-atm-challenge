package com.link.atm.exception;

public class AccountNotFoundException extends AtmException {
    public AccountNotFoundException(String accountIdentifier) {
        super("La cuenta no existe", 404);
    }
}
