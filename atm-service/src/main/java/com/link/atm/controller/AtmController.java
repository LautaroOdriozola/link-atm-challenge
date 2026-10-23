package com.link.atm.controller;

import com.link.atm.dto.ApiResponse;
import com.link.atm.dto.DepositRequest;
import com.link.atm.dto.LoginRequest;
import com.link.atm.dto.WithdrawRequest;
import com.link.atm.exception.AtmException;
import com.link.atm.service.AtmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class AtmController {

    private final AtmService atmService;

    public AtmController(AtmService atmService) {
        this.atmService = atmService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            atmService.login(request.getCardNumber());
            return ResponseEntity.ok(ApiResponse.ok("Ingreso exitoso"));
        } catch (AtmException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.error("Ingreso no exitoso"));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse> withdraw(@RequestBody WithdrawRequest request) {
        BigDecimal resultingBalance = atmService.withdraw(
                request.getCardNumber(), request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.ok("Retire su dinero", resultingBalance));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse> deposit(@RequestBody DepositRequest request) {
        BigDecimal resultingBalance = atmService.deposit(
                request.getCardNumber(), request.getCbu(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.ok("Deposito exitoso", resultingBalance));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse> getBalance(@RequestParam String cardNumber,
                                                   @RequestParam String accountNumber) {
        BigDecimal balance = atmService.getBalance(cardNumber, accountNumber);
        return ResponseEntity.ok(ApiResponse.ok("Consulta de saldo exitosa", balance));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(ApiResponse.ok("atm-service UP"));
    }
}
