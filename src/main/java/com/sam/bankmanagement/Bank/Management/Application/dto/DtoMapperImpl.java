package com.sam.bankmanagement.Bank.Management.Application.dto;

import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapperImpl implements DtoMapper {

    @Override
    public CustomerDto toCustomerDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .nationalId(customer.getNationalId())
                .gender(customer.getGender())
                .status(customer.getStatus())
                .accounts(toAccountDtos(customer.getAccounts()))
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    @Override
    public List<CustomerDto> toCustomerDtos(List<Customer> customers) {
        if (customers == null) {
            return null;
        }

        return customers.stream()
                .map(this::toCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDto toCustomerDtoWithoutAccounts(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .nationalId(customer.getNationalId())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    @Override
    public AccountDto toAccountDto(Account account) {
        if (account == null) {
            return null;
        }

        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .interestRate(account.getInterestRate())
                .accruedInterest(account.getAccruedInterest())
                .lastInterestCalculated(account.getLastInterestCalculated())
                .status(account.getStatus())
                .customerId(account.getCustomer() != null ? account.getCustomer().getId() : null)
                .customerName(getCustomerFullName(account.getCustomer()))
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    public List<AccountDto> toAccountDtos(List<Account> accounts) {
        if (accounts == null) {
            return null;
        }

        return accounts.stream()
                .map(this::toAccountDto)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDto toAccountDtoWithoutCustomer(Account account) {
        if (account == null) {
            return null;
        }

        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .interestRate(account.getInterestRate())
                .accruedInterest(account.getAccruedInterest())
                .lastInterestCalculated(account.getLastInterestCalculated())
                .status(account.getStatus())
                .customerId(account.getCustomer() != null ? account.getCustomer().getId() : null)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    public TransactionDto toTransactionDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionDto.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .fromAccountNumber(transaction.getFromAccount() != null ?
                        transaction.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(transaction.getToAccount() != null ?
                        transaction.getToAccount().getAccountNumber() : null)
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }

    @Override
    public List<TransactionDto> toTransactionDtos(List<Transaction> transactions) {
        if (transactions == null) {
            return null;
        }

        return transactions.stream()
                .map(this::toTransactionDto)
                .collect(Collectors.toList());
    }

    public String getCustomerFullName(Customer customer) {
        if (customer == null) {
            return null;
        }
        return customer.getFirstName() + " " + customer.getLastName();
    }
}
