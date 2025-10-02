package com.sam.bankmanagement.Bank.Management.Application.service;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import com.sam.bankmanagement.Bank.Management.Application.repository.AccountRepository;
import com.sam.bankmanagement.Bank.Management.Application.repository.CustomerRepository;
import com.sam.bankmanagement.Bank.Management.Application.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final DtoMapper dtoMapper;


    @Transactional(readOnly = true)
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        log.info("Admin fetching all customers with pagination");
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(dtoMapper::toCustomerDtoWithoutAccounts);
    }


    @Transactional
    public CustomerDto updateCustomerStatus(Long customerId, Customer.CustomerStatus status) {
        log.info("Admin updating customer status for ID: {} to {}", customerId, status);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        customer.setStatus(status);
        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer status updated successfully by admin for ID: {}", customerId);
        return dtoMapper.toCustomerDto(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        log.info("Admin attempting to delete customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        customerRepository.delete(customer);
        log.info("Customer with ID: {} successfully deleted by admin", customerId);
    }


    @Transactional(readOnly = true)
    public Page<AccountDto> getAllAccounts(Pageable pageable) {
        log.info("Admin fetching all accounts with pagination");
        Page<Account> accounts = accountRepository.findAll(pageable);
        return accounts.map(dtoMapper::toAccountDto);
    }

    @Transactional
    public AccountDto updateAccountStatus(Long accountId, Account.AccountStatus status) {
        log.info("Admin updating account status for ID: {} to {}", accountId, status);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setStatus(status);
        Account updatedAccount = accountRepository.save(account);

        log.info("Account status updated successfully by admin for ID: {}", accountId);
        return dtoMapper.toAccountDto(updatedAccount);
    }

    @Transactional
    public AccountDto updateAccountInterestRate(Long accountId, BigDecimal interestRate) {
        log.info("Admin updating interest rate for account ID: {} to {}", accountId, interestRate);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setInterestRate(interestRate);
        Account updatedAccount = accountRepository.save(account);

        log.info("Account interest rate updated successfully by admin for ID: {}", accountId);
        return dtoMapper.toAccountDto(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        log.info("Admin attempting to delete account with ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        List<Transaction> pendingTransactions = transactionRepository.findByFromAccountIdAndStatus(accountId, Transaction.TransactionStatus.PENDING);
        pendingTransactions.addAll(transactionRepository.findByToAccountIdAndStatus(accountId, Transaction.TransactionStatus.PENDING));

        if (!pendingTransactions.isEmpty()) {
            log.error("Cannot delete account ID: {} - has {} pending transactions", accountId, pendingTransactions.size());
            throw new IllegalStateException("Cannot delete account with pending transactions. Please complete or cancel all pending transactions first.");
        }
        accountRepository.delete(account);
        log.info("Account with ID: {} successfully deleted by admin", accountId);
    }

    // Transaction Management
    @Transactional(readOnly = true)
    public Page<TransactionDto> getAllTransactions(Pageable pageable) {
        log.info("Admin fetching all transactions with pagination");
        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        return transactions.map(dtoMapper::toTransactionDto);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long transactionId) {
        log.info("Admin fetching transaction with ID: {}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        return dtoMapper.toTransactionDto(transaction);
    }


    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByAccountNumber(String accountNumber, Pageable pageable) {
        log.info("Admin fetching transactions for account number: {}", accountNumber);
        // Verify account exists
        accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        Page<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber, pageable);
        return transactions.map(dtoMapper::toTransactionDto);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByCustomerId(Long customerId, Pageable pageable) {
        log.info("Admin fetching transactions for customer ID: {}", customerId);
        // Verify customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Page<Transaction> transactions = transactionRepository.findByCustomerId(customerId, pageable);
        return transactions.map(dtoMapper::toTransactionDto);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Admin fetching transactions between {} and {}", startDate, endDate);
        Page<Transaction> transactions = transactionRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        return transactions.map(dtoMapper::toTransactionDto);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByStatus(Transaction.TransactionStatus status, Pageable pageable) {
        log.info("Admin fetching transactions with status: {}", status);
        Page<Transaction> transactions = transactionRepository.findByStatus(status, pageable);
        return transactions.map(dtoMapper::toTransactionDto);
    }
}
