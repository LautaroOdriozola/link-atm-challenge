package com.link.atm.exception;

public class InvalidRequestException extends AtmException {
    public InvalidRequestException(String detail) {
        super(detail, 400);
    }
}
