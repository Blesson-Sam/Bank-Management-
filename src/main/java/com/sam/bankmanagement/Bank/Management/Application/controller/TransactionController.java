package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.DepositWithdrawRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransferRequest;
import com.sam.bankmanagement.Bank.Management.Application.service.TransactionService;
import com.sam.bankmanagement.Bank.Management.Application.service.AccountService;
import com.sam.bankmanagement.Bank.Management.Application.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing banking transactions")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money", description = "Deposits money into your own account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deposit successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<TransactionDto> deposit(@Valid @RequestBody DepositWithdrawRequest request) {
        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();

        if (!isAccountOwnedByCustomer(request.getAccountNumber(), currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TransactionDto transactionDto = transactionService.deposit(request);
        return new ResponseEntity<>(transactionDto, HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraws money from your own account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Withdrawal successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<TransactionDto> withdraw(@Valid @RequestBody DepositWithdrawRequest request) {
        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();

        if (!isAccountOwnedByCustomer(request.getAccountNumber(), currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TransactionDto transactionDto = transactionService.withdraw(request);
        return new ResponseEntity<>(transactionDto, HttpStatus.CREATED);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfers money from your account to another account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<TransactionDto> transfer(@Valid @RequestBody TransferRequest request) {
        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();

        if (!isAccountOwnedByCustomer(request.getFromAccountNumber(), currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TransactionDto transactionDto = transactionService.transfer(request);
        return new ResponseEntity<>(transactionDto, HttpStatus.CREATED);
    }

    @GetMapping("/my-transactions")
    @Operation(summary = "Get all my transactions", description = "Retrieves all transactions for the authenticated customer's accounts")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<List<TransactionDto>> getMyTransactions() {
        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();
        List<com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto> customerAccounts = accountService.getAccountsByCustomerId(currentCustomerId);
        List<TransactionDto> allTransactions = new ArrayList<>();

        for (com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto account : customerAccounts) {
            List<TransactionDto> accountTransactions = transactionService.getTransactionsByAccountNumber(account.getAccountNumber());
            allTransactions.addAll(accountTransactions);
        }

        return ResponseEntity.ok(allTransactions);
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get transactions for my account", description = "Retrieves all transactions for a specific account (only if owned)")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<List<TransactionDto>> getTransactionsByAccountNumber(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {

        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();

        if (!isAccountOwnedByCustomer(accountNumber, currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TransactionDto> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountNumber}/paginated")
    @Operation(summary = "Get paginated transactions for my account", description = "Retrieves paginated transactions for a specific account (only if owned)")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByAccountIdPaginated(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();

        if (!isAccountOwnedByCustomer(accountNumber, currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AccountDto account = accountService.getAccountByNumber(accountNumber);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TransactionDto> transactions = transactionService.getTransactionsByAccountId(account.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountNumber}/date-range")
    @Operation(summary = "Get transactions by date range for my account", description = "Retrieves transactions for your account within a date range")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<List<TransactionDto>> getTransactionsByDateRange(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            @Parameter(description = "Start date (yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();

        if (!isAccountOwnedByCustomer(accountNumber, currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TransactionDto> transactions = transactionService.getTransactionsByAccountAndDateRange(
                accountNumber, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get my transaction by ID", description = "Retrieves transaction details by ID (only if it involves your accounts)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionDto> getTransactionById(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long id) {

        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();
        TransactionDto transactionDto = transactionService.getTransactionById(id);

        if (!isTransactionOwnedByCustomer(transactionDto, currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(transactionDto);
    }

    @GetMapping("/transaction-id/{transactionId}")
    @Operation(summary = "Get my transaction by transaction ID", description = "Retrieves transaction details by unique transaction ID (only if it involves your accounts)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionDto> getTransactionByTransactionId(
            @Parameter(description = "Unique transaction ID", required = true)
            @PathVariable String transactionId) {

        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();
        TransactionDto transactionDto = transactionService.getTransactionByTransactionId(transactionId);

        if (!isTransactionOwnedByCustomer(transactionDto, currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(transactionDto);
    }

    private boolean isAccountOwnedByCustomer(String accountNumber, Long customerId) {
        try {
            AccountDto account = accountService.getAccountByNumber(accountNumber);
            return account.getCustomerId().equals(customerId);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTransactionOwnedByCustomer(TransactionDto transaction, Long customerId) {

        boolean ownsFromAccount = transaction.getFromAccountNumber() != null &&
            isAccountOwnedByCustomer(transaction.getFromAccountNumber(), customerId);

        boolean ownsToAccount = transaction.getToAccountNumber() != null &&
            isAccountOwnedByCustomer(transaction.getToAccountNumber(), customerId);

        return ownsFromAccount || ownsToAccount;
    }
}
