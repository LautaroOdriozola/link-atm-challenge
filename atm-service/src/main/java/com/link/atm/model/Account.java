package com.link.atm.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {

    @Getter
    private final String number;
    @Getter
    private final String cbu;
    private BigDecimal balance;

    public Account(String number, String cbu, BigDecimal initialBalance) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be blank");
        }
        if (cbu == null || cbu.trim().isEmpty()) {
            throw new IllegalArgumentException("CBU must not be blank");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance must not be negative");
        }
        this.number = number;
        this.cbu = cbu;
        this.balance = initialBalance;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }

    public synchronized BigDecimal debit(BigDecimal amount) {
        BigDecimal resultingBalance = balance.subtract(amount);
        if (resultingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        balance = resultingBalance;
        return balance;
    }

    public synchronized BigDecimal credit(BigDecimal amount) {
        balance = balance.add(amount);
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(number, account.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Account{number='" + number + "', cbu='" + cbu + "', balance=" + balance + "}";
    }
}
