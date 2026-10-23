package com.link.atm.repository.impl;

import com.link.atm.model.Account;
import com.link.atm.repository.AccountRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> accountsByNumber = new ConcurrentHashMap<>();
    private final Map<String, Account> accountsByCbu = new ConcurrentHashMap<>();

    public InMemoryAccountRepository() {
        loadSampleData();
    }

    private void loadSampleData() {
        save(new Account("ACC-0001", "0720001288000001234567", new BigDecimal("50000.00")));
        save(new Account("ACC-0002", "0720001288000007654321", new BigDecimal("1000.50")));
        save(new Account("ACC-0003", "0720001288000009999999", new BigDecimal("99999.99")));
    }

    private void save(Account account) {
        accountsByNumber.put(account.getNumber(), account);
        accountsByCbu.put(account.getCbu(), account);
    }

    @Override
    public Optional<Account> findByNumber(String number) {
        return Optional.ofNullable(accountsByNumber.get(number));
    }

    @Override
    public Optional<Account> findByCbu(String cbu) {
        return Optional.ofNullable(accountsByCbu.get(cbu));
    }
}
