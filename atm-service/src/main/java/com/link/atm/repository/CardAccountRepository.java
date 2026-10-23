package com.link.atm.repository;

import java.util.List;

public interface CardAccountRepository {

    boolean existsLink(String cardNumber, String accountNumber);

    List<String> accountsForCard(String cardNumber);
}
