package com.sam.bankmanagement.Bank.Management.Application.service;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CreateAccountRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.exception.AccountStatusException;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import com.sam.bankmanagement.Bank.Management.Application.repository.AccountRepository;
import com.sam.bankmanagement.Bank.Management.Application.repository.CustomerRepository;
import com.sam.bankmanagement.Bank.Management.Application.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final DtoMapper dtoMapper;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository, TransactionRepository transactionRepository, DtoMapper dtoMapper) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.dtoMapper = dtoMapper;
    }

    @Value("${app.interest.savings-rate:3.5}")
    private double savingsInterestRate;

    @Value("${app.interest.current-rate:0.5}")
    private double currentInterestRate;

    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        log.info("Creating account for customer ID: {}", request.getCustomerId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        if (customer.getStatus() != Customer.CustomerStatus.ACTIVE) {
            throw new AccountStatusException("Cannot create account for inactive customer");
        }

        String accountNumber = generateAccountNumber();

        BigDecimal interestRate = request.getCustomInterestRate() != null ?
                request.getCustomInterestRate() :
                BigDecimal.valueOf(request.getAccountType().getDefaultInterestRate());

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO)
                .interestRate(interestRate)
                .accruedInterest(BigDecimal.ZERO)
                .lastInterestCalculated(LocalDateTime.now())
                .status(Account.AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        Account savedAccount = accountRepository.save(account);

        if (request.getInitialDeposit() != null && request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            Transaction depositTransaction = Transaction.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .type(Transaction.TransactionType.DEPOSIT)
                    .amount(request.getInitialDeposit())
                    .description("Initial deposit")
                    .toAccount(savedAccount)
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .completedAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(depositTransaction);
        }

        log.info("Account created successfully with number: {}", accountNumber);
        return dtoMapper.toAccountDto(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountByNumber(String accountNumber) {
        log.info("Fetching account with number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        return dtoMapper.toAccountDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByCustomerId(Long customerId) {
        log.info("Fetching accounts for customer ID: {}", customerId);
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        return dtoMapper.toAccountDtos(accounts);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAllAccounts() {
        log.info("Fetching all accounts");
        List<Account> accounts = accountRepository.findAll();
        return dtoMapper.toAccountDtos(accounts);
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getAllAccounts(Pageable pageable) {
        log.info("Fetching all accounts with pagination");
        Page<Account> accounts = accountRepository.findAll(pageable);
        return accounts.map(dtoMapper::toAccountDto);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountById(Long accountId) {
        log.info("Fetching account with ID: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        return dtoMapper.toAccountDto(account);
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getAccountsByCustomerId(Long customerId, Pageable pageable) {
        log.info("Fetching accounts for customer ID: {} with pagination", customerId);
        Page<Account> accounts = accountRepository.findByCustomerId(customerId, pageable);
        return accounts.map(dtoMapper::toAccountDto);
    }

    @Transactional
    public AccountDto updateAccountStatus(String accountNumber, Account.AccountStatus status) {
        log.info("Updating account status for number: {} to {}", accountNumber, status);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        account.setStatus(status);
        Account updatedAccount = accountRepository.save(account);

        log.info("Account status updated successfully for number: {}", accountNumber);
        return dtoMapper.toAccountDto(updatedAccount);
    }

    @Transactional
    public AccountDto updateInterestRate(String accountNumber, BigDecimal newRate) {
        log.info("Updating interest rate for account: {} to {}", accountNumber, newRate);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        account.setInterestRate(newRate);
        Account updatedAccount = accountRepository.save(account);

        log.info("Interest rate updated successfully for account: {}", accountNumber);
        return dtoMapper.toAccountDto(updatedAccount);
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void calculateDailyInterest() {
        log.info("Starting daily interest calculation");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1);
        List<Account> accounts = accountRepository.
                findAccountsForInterestCalculation(cutoffDate);

        for (Account account : accounts) {
            calculateInterestForAccount(account);
        }

        log.info("Completed daily interest calculation for {} accounts", accounts.size());
    }

    private void calculateInterestForAccount(Account account) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Calculate daily interest = (balance * annual_rate) / 365
        BigDecimal dailyRate = account.getInterestRate().divide(BigDecimal.valueOf(365 * 100), 8, RoundingMode.HALF_UP);
        BigDecimal dailyInterest = account.getBalance().multiply(dailyRate).setScale(2, RoundingMode.HALF_UP);

        account.setAccruedInterest(account.getAccruedInterest().add(dailyInterest));
        account.setLastInterestCalculated(LocalDateTime.now());

        accountRepository.save(account);

        log.debug("Interest calculated for account {}: {}", account.getAccountNumber(), dailyInterest);
    }

    @Transactional
    public void creditAccruedInterest(String accountNumber) {
        log.info("Crediting accrued interest for account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        if (account.getAccruedInterest().compareTo(BigDecimal.ZERO) > 0) {
            account.setBalance(account.getBalance().add(account.getAccruedInterest()));

            Transaction interestTransaction = Transaction.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .type(Transaction.TransactionType.INTEREST_CREDIT)
                    .amount(account.getAccruedInterest())
                    .description("Interest credit")
                    .toAccount(account)
                    .status(Transaction.TransactionStatus.COMPLETED)
                    .completedAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(interestTransaction);

            account.setAccruedInterest(BigDecimal.ZERO);
            accountRepository.save(account);

            log.info("Accrued interest credited successfully for account: {}", accountNumber);
        }
    }

    private String generateAccountNumber() {
        String prefix = "ACC";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + timestamp.substring(timestamp.length() - 6) + randomPart;
    }
}
