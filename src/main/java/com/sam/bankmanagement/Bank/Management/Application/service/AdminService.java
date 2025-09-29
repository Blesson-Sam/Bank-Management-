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

    // Customer Management
    @Transactional(readOnly = true)
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        log.info("Admin fetching all customers with pagination");
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(dtoMapper::toCustomerDtoWithoutAccounts);
    }

//    @Transactional(readOnly = true)
//    public CustomerDto getCustomerById(Long customerId) {
//        log.info("Admin fetching customer with ID: {}", customerId);
//        Customer customer = customerRepository.findById(customerId)
//                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
//        return dtoMapper.toCustomerDto(customer);
//    }

    @Transactional(readOnly = true)
    public List<CustomerDto> searchCustomers(String searchTerm) {
        log.info("Admin searching customers with term: {}", searchTerm);
        List<Customer> customers = customerRepository.searchCustomers(searchTerm);
        return customers.stream()
                .map(dtoMapper::toCustomerDtoWithoutAccounts)
                .collect(Collectors.toList());
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

    // Account Management
    @Transactional(readOnly = true)
    public Page<AccountDto> getAllAccounts(Pageable pageable) {
        log.info("Admin fetching all accounts with pagination");
        Page<Account> accounts = accountRepository.findAll(pageable);
        return accounts.map(dtoMapper::toAccountDto);
    }

//    @Transactional(readOnly = true)
//    public AccountDto getAccountById(Long accountId) {
//        log.info("Admin fetching account with ID: {}", accountId);
//        Account account = accountRepository.findById(accountId)
//                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
//        return dtoMapper.toAccountDto(account);
//    }

//    @Transactional(readOnly = true)
//    public AccountDto getAccountByNumber(String accountNumber) {
//        log.info("Admin fetching account with number: {}", accountNumber);
//        Account account = accountRepository.findByAccountNumber(accountNumber)
//                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
//        return dtoMapper.toAccountDto(account);
//    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getAccountsByCustomerId(Long customerId, Pageable pageable) {
        log.info("Admin fetching accounts for customer ID: {}", customerId);
        // Verify customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Page<Account> accounts = accountRepository.findByCustomerId(customerId, pageable);
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

//    @Transactional(readOnly = true)
//    public TransactionDto getTransactionByTransactionId(String transactionId) {
//        log.info("Admin fetching transaction with transaction ID: {}", transactionId);
//        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
//                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "transactionId", transactionId));
//        return dtoMapper.toTransactionDto(transaction);
//    }

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

    // Dashboard and Analytics
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        log.info("Admin fetching dashboard statistics");

        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.countByStatus(Customer.CustomerStatus.ACTIVE);
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.countByStatus(Account.AccountStatus.ACTIVE);
        long totalTransactions = transactionRepository.count();
        long todayTransactions = transactionRepository.countByCreatedAtAfter(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));

        BigDecimal totalBalance = accountRepository.findAll().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "totalCustomers", totalCustomers,
                "activeCustomers", activeCustomers,
                "totalAccounts", totalAccounts,
                "activeAccounts", activeAccounts,
                "totalTransactions", totalTransactions,
                "todayTransactions", todayTransactions,
                "totalBalance", totalBalance
        );
    }



//    @Transactional(readOnly = true)
//    public List<Map<String, Object>> getAccountTypeDistribution() {
//        log.info("Admin fetching account type distribution");
//        List<Account> accounts = accountRepository.findAll();
//
//        Map<String, Long> distribution = accounts.stream()
//                .collect(Collectors.groupingBy(
//                        account -> account.getAccountType().toString(),
//                        Collectors.counting()
//                ));
//
//        return distribution.entrySet().stream()
//                .map(entry -> Map.<String, Object>of(
//                        "accountType", entry.getKey(),
//                        "count", entry.getValue()
//                ))
//                .collect(Collectors.toList());
//    }

    // System Management
    @Transactional
    public void processInterestForAllAccounts() {
        log.info("Admin processing interest for all eligible accounts");
        List<Account> savingsAccounts = accountRepository.findByAccountTypeAndStatus(
                Account.AccountType.SAVINGS, Account.AccountStatus.ACTIVE);

        for (Account account : savingsAccounts) {
            if (account.getLastInterestCalculated() == null ||
                account.getLastInterestCalculated().isBefore(LocalDateTime.now().minusMonths(1))) {

                BigDecimal interest = account.getBalance()
                        .multiply(account.getInterestRate())
                        .divide(BigDecimal.valueOf(100));

                account.setBalance(account.getBalance().add(interest));
                account.setAccruedInterest(account.getAccruedInterest().add(interest));
                account.setLastInterestCalculated(LocalDateTime.now());

                accountRepository.save(account);
                log.info("Interest processed for account {}: {}", account.getAccountNumber(), interest);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getInactiveAccounts() {
        log.info("Admin fetching inactive accounts");
        List<Account> inactiveAccounts = accountRepository.findByStatus(Account.AccountStatus.INACTIVE);
        return inactiveAccounts.stream()
                .map(dtoMapper::toAccountDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getPendingTransactions() {
        log.info("Admin fetching pending transactions");
        List<Transaction> pendingTransactions = transactionRepository.findByStatus(Transaction.TransactionStatus.PENDING);
        return pendingTransactions.stream()
                .map(dtoMapper::toTransactionDto)
                .collect(Collectors.toList());
    }
}
