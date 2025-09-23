package com.sam.bankmanagement.Bank.Management.Application.service;

import com.sam.bankmanagement.Bank.Management.Application.dto.DepositWithdrawRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransferRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.exception.AccountStatusException;
import com.sam.bankmanagement.Bank.Management.Application.exception.InsufficientFundsException;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import com.sam.bankmanagement.Bank.Management.Application.repository.AccountRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomerService.class);


    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final DtoMapper dtoMapper;

    private static final long MAX_DAILY_TRANSACTIONS = 50;
    private static final BigDecimal MAX_DAILY_WITHDRAWAL = new BigDecimal("10000.00");

    @Transactional
    public TransactionDto transfer(TransferRequest request) {
        log.info("Processing transfer from: {} to: {} amount: {}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Account fromAccount = getActiveAccount(request.getFromAccountNumber());
        Account toAccount = getActiveAccount(request.getToAccountNumber());

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(fromAccount.getAccountNumber(), request.getAmount(), fromAccount.getBalance());
        }

        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .type(Transaction.TransactionType.TRANSFER)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Transfer")
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        try {

            fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            savedTransaction.setCompletedAt(LocalDateTime.now());
            savedTransaction = transactionRepository.save(savedTransaction);

            log.info("Transfer completed successfully. Transaction ID: {}", savedTransaction.getTransactionId());

        } catch (Exception e) {
            log.error("Transfer failed for transaction: {}", savedTransaction.getTransactionId(), e);
            savedTransaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            throw new RuntimeException("Transfer processing failed", e);
        }

        return dtoMapper.toTransactionDto(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long id) {
        log.info("Fetching transaction with ID: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return dtoMapper.toTransactionDto(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionByTransactionId(String transactionId) {
        log.info("Fetching transaction with transaction ID: {}", transactionId);
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "transactionId", transactionId));
        return dtoMapper.toTransactionDto(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        log.info("Fetching transactions for account ID: {}", accountId);
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        return transactions.map(dtoMapper::toTransactionDto);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByAccountNumber(String accountNumber) {
        log.info("Fetching transactions for account number: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        return dtoMapper.toTransactionDtos(transactions);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByAccountAndDateRange(String accountNumber,
                                                                     LocalDateTime startDate,
                                                                     LocalDateTime endDate) {
        log.info("Fetching transactions for account: {} between {} and {}", accountNumber, startDate, endDate);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(
                account.getId(), startDate, endDate);
        return dtoMapper.toTransactionDtos(transactions);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getAllTransactions() {
        log.info("Fetching all transactions");
        List<Transaction> transactions = transactionRepository.findAll();
        return dtoMapper.toTransactionDtos(transactions);
    }

    private Account getActiveAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new AccountStatusException("Account " + accountNumber + " is not active. Current status: " + account.getStatus());
        }

        return account;
    }


    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis()
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Transactional
    public TransactionDto deposit(DepositWithdrawRequest request) {
        log.info("Processing deposit for account: {} amount: {}", request.getAccountNumber(), request.getAmount());

        Account account = getActiveAccount(request.getAccountNumber());

        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Deposit")
                .toAccount(account)
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        try {

            account.setBalance(account.getBalance().add(request.getAmount()));
            accountRepository.save(account);

            savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            savedTransaction.setCompletedAt(LocalDateTime.now());
            savedTransaction = transactionRepository.save(savedTransaction);

            log.info("Deposit completed successfully. Transaction ID: {}", savedTransaction.getTransactionId());
        } catch (Exception e) {
            log.error("Deposit failed for transaction: {}", savedTransaction.getTransactionId(), e);
            savedTransaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            throw new RuntimeException("Deposit processing failed", e);
        }

        return dtoMapper.toTransactionDto(savedTransaction);
    }

    @Transactional
    public TransactionDto withdraw(DepositWithdrawRequest request) {
        log.info("Processing withdrawal for account: {} amount: {}", request.getAccountNumber(), request.getAmount());

        Account account = getActiveAccount(request.getAccountNumber());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(account.getAccountNumber(), request.getAmount(), account.getBalance());
        }

        Transaction transaction = Transaction.builder()
                .transactionId(generateTransactionId())
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Withdrawal")
                .fromAccount(account)
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        try {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            accountRepository.save(account);
            savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            savedTransaction.setCompletedAt(LocalDateTime.now());
            savedTransaction = transactionRepository.save(savedTransaction);

            log.info("Withdrawal completed successfully. Transaction ID: {}", savedTransaction.getTransactionId());
        } catch (Exception e) {
            log.error("Withdrawal failed for transaction: {}", savedTransaction.getTransactionId(), e);
            savedTransaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            throw new RuntimeException("Withdrawal processing failed", e);
        }
        return dtoMapper.toTransactionDto(savedTransaction);
    }

}
