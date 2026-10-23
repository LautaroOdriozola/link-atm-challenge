package com.link.atm.exception;

public abstract class AtmException extends RuntimeException {

    private final int httpStatus;

    protected AtmException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
