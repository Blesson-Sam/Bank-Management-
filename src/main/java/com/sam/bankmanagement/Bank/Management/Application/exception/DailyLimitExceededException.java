package com.sam.bankmanagement.Bank.Management.Application.exception;

public class DailyLimitExceededException extends RuntimeException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}

