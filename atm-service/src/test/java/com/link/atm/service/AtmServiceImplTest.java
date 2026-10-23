package com.link.atm.service;

import com.link.atm.exception.AccountNotLinkedException;
import com.link.atm.exception.InactiveCardException;
import com.link.atm.exception.InsufficientBalanceException;
import com.link.atm.logging.TransactionLogger;
import com.link.atm.model.Account;
import com.link.atm.model.Card;
import com.link.atm.repository.AccountRepository;
import com.link.atm.repository.CardAccountRepository;
import com.link.atm.repository.CardRepository;
import com.link.atm.service.impl.AtmServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.link.atm.logging.TransactionLogger.Result.ERROR;
import static com.link.atm.logging.TransactionLogger.Result.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtmServiceImplTest {

    private static final String CARD = "4111111111111111";
    private static final String ACCOUNT = "ACC-0001";
    private static final String CBU = "0720001288000009999999";

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardAccountRepository cardAccountRepository;

    @Mock
    private TransactionLogger transactionLogger;

    @InjectMocks
    private AtmServiceImpl atmService;

    @Test
    void login_withActiveCard_logsSuccess() {
        when(cardRepository.findByNumber(CARD)).thenReturn(Optional.of(new Card(CARD, true)));

        atmService.login(CARD);

        verify(transactionLogger).log("LOGIN", CARD, null, null, OK, "Ingreso exitoso", null);
    }

    @Test
    void login_withInactiveCard_throwsAndLogsError() {
        when(cardRepository.findByNumber(CARD)).thenReturn(Optional.of(new Card(CARD, false)));

        assertThrows(InactiveCardException.class, () -> atmService.login(CARD));

        verify(transactionLogger).log(eq("LOGIN"), eq(CARD), isNull(), isNull(), eq(ERROR), anyString(), isNull());
    }

    @Test
    void withdraw_withEnoughBalance_returnsResultingBalance() {
        when(cardRepository.findByNumber(CARD)).thenReturn(Optional.of(new Card(CARD, true)));
        when(cardAccountRepository.existsLink(CARD, ACCOUNT)).thenReturn(true);
        when(accountRepository.findByNumber(ACCOUNT))
                .thenReturn(Optional.of(new Account(ACCOUNT, CBU, new BigDecimal("1000.00"))));

        BigDecimal resultingBalance = atmService.withdraw(CARD, ACCOUNT, new BigDecimal("400.00"));

        assertEquals(new BigDecimal("600.00"), resultingBalance);
    }

    @Test
    void withdraw_withInsufficientBalance_throws() {
        when(cardRepository.findByNumber(CARD)).thenReturn(Optional.of(new Card(CARD, true)));
        when(cardAccountRepository.existsLink(CARD, ACCOUNT)).thenReturn(true);
        when(accountRepository.findByNumber(ACCOUNT))
                .thenReturn(Optional.of(new Account(ACCOUNT, CBU, new BigDecimal("100.00"))));

        assertThrows(InsufficientBalanceException.class,
                () -> atmService.withdraw(CARD, ACCOUNT, new BigDecimal("100.01")));
    }

    @Test
    void deposit_intoExistingCbu_returnsResultingBalance() {
        when(cardRepository.findByNumber(CARD)).thenReturn(Optional.of(new Card(CARD, true)));
        when(accountRepository.findByCbu(CBU))
                .thenReturn(Optional.of(new Account("ACC-0003", CBU, new BigDecimal("50.00"))));

        BigDecimal resultingBalance = atmService.deposit(CARD, CBU, new BigDecimal("25.50"));

        assertEquals(new BigDecimal("75.50"), resultingBalance);
    }

    @Test
    void getBalance_ofAccountNotLinkedToCard_throws() {
        when(cardRepository.findByNumber(CARD)).thenReturn(Optional.of(new Card(CARD, true)));
        when(cardAccountRepository.existsLink(CARD, ACCOUNT)).thenReturn(false);

        assertThrows(AccountNotLinkedException.class, () -> atmService.getBalance(CARD, ACCOUNT));

        verify(accountRepository, never()).findByNumber(any());
    }
}
