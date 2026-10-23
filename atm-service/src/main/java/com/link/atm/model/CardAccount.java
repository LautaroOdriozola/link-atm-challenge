package com.link.atm.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public class CardAccount {

    private final String cardNumber;
    private final String accountNumber;

    public CardAccount(String cardNumber, String accountNumber) {
        this.cardNumber = cardNumber;
        this.accountNumber = accountNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardAccount)) return false;
        CardAccount that = (CardAccount) o;
        return Objects.equals(cardNumber, that.cardNumber) && Objects.equals(accountNumber, that.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, accountNumber);
    }

    @Override
    public String toString() {
        return "CardAccount{cardNumber='" + cardNumber + "', accountNumber='" + accountNumber + "'}";
    }
}
