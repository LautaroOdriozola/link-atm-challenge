package com.link.atm.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ApiResponse {

    private final boolean success;
    private final String message;
    private final BigDecimal balance;

    private ApiResponse(boolean success, String message, BigDecimal balance) {
        this.success = success;
        this.message = message;
        this.balance = balance;
    }

    public static ApiResponse ok(String message) {
        return new ApiResponse(true, message, null);
    }

    public static ApiResponse ok(String message, BigDecimal balance) {
        return new ApiResponse(true, message, balance);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null);
    }
}
