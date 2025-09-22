package com.sam.bankmanagement.Bank.Management.Application.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountNumber, BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format("Insufficient funds in account %s. Requested: %s, Available: %s",
                accountNumber, requestedAmount, availableBalance));
    }

    public InsufficientFundsException(String message) {
        super(message);
    }
}

