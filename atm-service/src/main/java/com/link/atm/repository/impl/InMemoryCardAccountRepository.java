package com.link.atm.repository.impl;

import com.link.atm.model.CardAccount;
import com.link.atm.repository.CardAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryCardAccountRepository implements CardAccountRepository {

    private final List<CardAccount> links = new CopyOnWriteArrayList<>();

    public InMemoryCardAccountRepository() {
        loadSampleData();
    }

    private void loadSampleData() {
        links.add(new CardAccount("4111111111111111", "ACC-0001"));
        links.add(new CardAccount("4333333333333333", "ACC-0002"));
    }

    @Override
    public boolean existsLink(String cardNumber, String accountNumber) {
        for (CardAccount link : links) {
            if (link.getCardNumber().equals(cardNumber) && link.getAccountNumber().equals(accountNumber)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> accountsForCard(String cardNumber) {
        List<String> result = new ArrayList<>();
        for (CardAccount link : links) {
            if (link.getCardNumber().equals(cardNumber)) {
                result.add(link.getAccountNumber());
            }
        }
        return Collections.unmodifiableList(result);
    }
}
