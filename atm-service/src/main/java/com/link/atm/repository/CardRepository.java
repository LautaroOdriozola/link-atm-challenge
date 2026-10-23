package com.link.atm.repository;

import com.link.atm.model.Card;

import java.util.Optional;

public interface CardRepository {

    Optional<Card> findByNumber(String number);
}
