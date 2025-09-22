package com.sam.bankmanagement.Bank.Management.Application.exception;

public class ActiveAccountsExistException extends RuntimeException {
    public ActiveAccountsExistException(Long id) {
        super("Customer with ID " + id + " has active accounts and cannot be deleted");
    }
}
