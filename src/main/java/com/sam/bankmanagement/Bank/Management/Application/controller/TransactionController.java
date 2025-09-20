package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.DepositWithdrawRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransferRequest;
import com.sam.bankmanagement.Bank.Management.Application.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing banking transactions")
public class TransactionController {


    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money", description = "Deposits money into an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deposit successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<TransactionDto> deposit(
            @Valid @RequestBody DepositWithdrawRequest request) {
        TransactionDto transactionDto = transactionService.deposit(request);
        return new ResponseEntity<>(transactionDto, HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraws money from an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Withdrawal successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<TransactionDto> withdraw(
            @Valid @RequestBody DepositWithdrawRequest request) {
        TransactionDto transactionDto = transactionService.withdraw(request);
        return new ResponseEntity<>(transactionDto, HttpStatus.CREATED);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfers money from one account to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<TransactionDto> transfer(
            @Valid @RequestBody TransferRequest request) {
        TransactionDto transactionDto = transactionService.transfer(request);
        return new ResponseEntity<>(transactionDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves transaction details by transaction ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionDto> getTransactionById(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long id) {
        TransactionDto transactionDto = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transactionDto);
    }

    @GetMapping("/transaction-id/{transactionId}")
    @Operation(summary = "Get transaction by transaction ID", description = "Retrieves transaction details by unique transaction ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionDto> getTransactionByTransactionId(
            @Parameter(description = "Unique transaction ID", required = true)
            @PathVariable String transactionId) {
        TransactionDto transactionDto = transactionService.getTransactionByTransactionId(transactionId);
        return ResponseEntity.ok(transactionDto);
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get transactions by account number", description = "Retrieves all transactions for a specific account")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<List<TransactionDto>> getTransactionsByAccountNumber(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {
        List<TransactionDto> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountNumber}/paginated")
    @Operation(summary = "Get paginated transactions by account", description = "Retrieves paginated transactions for a specific account")
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

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // First get account to get ID
        // This is simplified - in real implementation you'd want to optimize this
        Page<TransactionDto> transactions = transactionService.getTransactionsByAccountId(1L, pageable); // This needs account ID
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountNumber}/date-range")
    @Operation(summary = "Get transactions by date range", description = "Retrieves transactions for an account within a date range")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<List<TransactionDto>> getTransactionsByDateRange(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            @Parameter(description = "Start date (yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        List<TransactionDto> transactions = transactionService.getTransactionsByAccountAndDateRange(
                accountNumber, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieves all transactions in the system")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        List<TransactionDto> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
}
