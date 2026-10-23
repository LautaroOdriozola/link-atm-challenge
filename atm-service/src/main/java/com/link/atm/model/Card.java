package com.link.atm.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class Card {

    private final String number;
    @Setter
    private volatile boolean active;

    public Card(String number, boolean active) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number must not be blank");
        }
        this.number = number;
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return Objects.equals(number, card.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Card{number='" + number + "', active=" + active + "}";
    }
}
