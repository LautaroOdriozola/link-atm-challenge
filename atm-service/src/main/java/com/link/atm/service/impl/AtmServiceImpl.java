package com.link.atm.service.impl;

import com.link.atm.exception.AccountNotFoundException;
import com.link.atm.exception.AccountNotLinkedException;
import com.link.atm.exception.AtmException;
import com.link.atm.exception.CardNotFoundException;
import com.link.atm.exception.InactiveCardException;
import com.link.atm.exception.InsufficientBalanceException;
import com.link.atm.exception.InvalidAmountException;
import com.link.atm.exception.InvalidRequestException;
import com.link.atm.logging.TransactionLogger;
import com.link.atm.model.Account;
import com.link.atm.model.Card;
import com.link.atm.repository.AccountRepository;
import com.link.atm.repository.CardAccountRepository;
import com.link.atm.repository.CardRepository;
import com.link.atm.service.AtmService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.link.atm.logging.TransactionLogger.Result.ERROR;
import static com.link.atm.logging.TransactionLogger.Result.OK;

@Service
public class AtmServiceImpl implements AtmService {

    private static final String OP_LOGIN = "LOGIN";
    private static final String OP_WITHDRAW = "WITHDRAW";
    private static final String OP_DEPOSIT = "DEPOSIT";
    private static final String OP_GET_BALANCE = "GET_BALANCE";

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CardAccountRepository cardAccountRepository;
    private final TransactionLogger transactionLogger;

    public AtmServiceImpl(CardRepository cardRepository,
                           AccountRepository accountRepository,
                           CardAccountRepository cardAccountRepository,
                           TransactionLogger transactionLogger) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.cardAccountRepository = cardAccountRepository;
        this.transactionLogger = transactionLogger;
    }

    @Override
    public void login(String cardNumber) {
        try {
            requireActiveCard(cardNumber);
            transactionLogger.log(OP_LOGIN, cardNumber, null, null, OK, "Ingreso exitoso", null);
        } catch (AtmException e) {
            transactionLogger.log(OP_LOGIN, cardNumber, null, null, ERROR, e.getMessage(), null);
            throw e;
        }
    }

    @Override
    public BigDecimal withdraw(String cardNumber, String accountNumber, BigDecimal amount) {
        try {
            requireActiveCard(cardNumber);
            requireValidAmount(amount);
            Account account = resolveOwnAccount(cardNumber, accountNumber);

            BigDecimal resultingBalance;
            try {
                resultingBalance = account.debit(amount);
            } catch (IllegalStateException insufficientBalance) {
                throw new InsufficientBalanceException();
            }

            transactionLogger.log(OP_WITHDRAW, cardNumber, accountNumber, amount, OK,
                    "Retire su dinero", resultingBalance);
            return resultingBalance;
        } catch (AtmException e) {
            transactionLogger.log(OP_WITHDRAW, cardNumber, accountNumber, amount, ERROR,
                    e.getMessage(), null);
            throw e;
        }
    }

    @Override
    public BigDecimal deposit(String cardNumber, String cbu, BigDecimal amount) {
        try {
            requireActiveCard(cardNumber);
            requireValidAmount(amount);
            if (cbu == null || cbu.trim().isEmpty()) {
                throw new InvalidRequestException("El CBU es obligatorio");
            }
            Account account = accountRepository.findByCbu(cbu)
                    .orElseThrow(() -> new AccountNotFoundException(cbu));

            BigDecimal resultingBalance = account.credit(amount);

            transactionLogger.log(OP_DEPOSIT, cardNumber, cbu, amount, OK,
                    "Deposito exitoso", resultingBalance);
            return resultingBalance;
        } catch (AtmException e) {
            transactionLogger.log(OP_DEPOSIT, cardNumber, cbu, amount, ERROR,
                    e.getMessage(), null);
            throw e;
        }
    }

    @Override
    public BigDecimal getBalance(String cardNumber, String accountNumber) {
        try {
            requireActiveCard(cardNumber);
            Account account = resolveOwnAccount(cardNumber, accountNumber);
            BigDecimal balance = account.getBalance();

            transactionLogger.log(OP_GET_BALANCE, cardNumber, accountNumber, null, OK,
                    "Consulta de saldo exitosa", balance);
            return balance;
        } catch (AtmException e) {
            transactionLogger.log(OP_GET_BALANCE, cardNumber, accountNumber, null, ERROR,
                    e.getMessage(), null);
            throw e;
        }
    }

    private Card requireActiveCard(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new InvalidRequestException("El numero de tarjeta es obligatorio");
        }
        Card card = cardRepository.findByNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException(cardNumber));
        if (!card.isActive()) {
            throw new InactiveCardException(cardNumber);
        }
        return card;
    }

    private Account resolveOwnAccount(String cardNumber, String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new InvalidRequestException("El numero de cuenta es obligatorio");
        }
        if (!cardAccountRepository.existsLink(cardNumber, accountNumber)) {
            throw new AccountNotLinkedException(cardNumber, accountNumber);
        }
        return accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    private void requireValidAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("es obligatorio");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("debe ser mayor a cero");
        }
    }
}
