package com.link.atm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AtmServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(AtmServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AtmServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner logStartupInfo(@Value("${server.port:8080}") String port,
                                             @Value("${atm.transaction-log.path:logs/transactions.log}") String logPath) {
        return args -> {
            String banner = "\n==========================================================="
                    + "\n atm-service listening on http://localhost:" + port
                    + "\n Endpoints:"
                    + "\n   POST  /api/login     { \"cardNumber\": \"...\" }"
                    + "\n   POST  /api/withdraw  { \"cardNumber\": \"...\", \"accountNumber\": \"...\", \"amount\": 0 }"
                    + "\n   POST  /api/deposit   { \"cardNumber\": \"...\", \"cbu\": \"...\", \"amount\": 0 }"
                    + "\n   GET   /api/balance?cardNumber=...&accountNumber=..."
                    + "\n   GET   /api/health"
                    + "\n Transaction log: " + logPath
                    + "\n===========================================================";
            log.info(banner);
        };
    }
}
