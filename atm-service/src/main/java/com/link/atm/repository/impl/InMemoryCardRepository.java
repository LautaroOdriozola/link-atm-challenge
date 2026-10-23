package com.link.atm.repository.impl;

import com.link.atm.model.Card;
import com.link.atm.repository.CardRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCardRepository implements CardRepository {

    private final Map<String, Card> cardsByNumber = new ConcurrentHashMap<>();

    public InMemoryCardRepository() {
        loadSampleData();
    }

    private void loadSampleData() {
        save(new Card("4111111111111111", true));
        save(new Card("4333333333333333", true));
        save(new Card("4222222222222222", false));
    }

    private void save(Card card) {
        cardsByNumber.put(card.getNumber(), card);
    }

    @Override
    public Optional<Card> findByNumber(String number) {
        return Optional.ofNullable(cardsByNumber.get(number));
    }
}
