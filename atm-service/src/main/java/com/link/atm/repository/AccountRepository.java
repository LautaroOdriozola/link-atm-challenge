package com.link.atm.repository;

import com.link.atm.model.Account;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findByNumber(String number);

    Optional<Account> findByCbu(String cbu);
}
