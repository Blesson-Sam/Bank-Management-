package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get all customers")
    @GetMapping("/customers")
    public ResponseEntity<Page<CustomerDto>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerDto> customers = adminService.getAllCustomers(pageable);
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Update customer status")
    @PutMapping("/customers/{customerId}/status")
    public ResponseEntity<CustomerDto> updateCustomerStatus(
            @PathVariable Long customerId,
            @RequestParam Customer.CustomerStatus status) {
        CustomerDto customer = adminService.updateCustomerStatus(customerId, status);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Delete a customer")
    @DeleteMapping("/customers/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long customerId) {
        try {
            adminService.deleteCustomer(customerId);
            return ResponseEntity.ok("Customer deleted successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while deleting the customer: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all accounts")
    @GetMapping("/accounts")
    public ResponseEntity<Page<AccountDto>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountDto> accounts = adminService.getAllAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }


    @Operation(summary = "Get accounts by customer ID")
    @GetMapping("/customers/{customerId}/accounts")
    public ResponseEntity<Page<AccountDto>> getAccountsByCustomerId(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountDto> accounts = adminService.getAccountsByCustomerId(customerId, pageable);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Update account status")
    @PutMapping("/accounts/{accountId}/status")
    public ResponseEntity<AccountDto> updateAccountStatus(
            @PathVariable Long accountId,
            @RequestParam Account.AccountStatus status) {
        AccountDto account = adminService.updateAccountStatus(accountId, status);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Update account interest rate")
    @PutMapping("/accounts/{accountId}/interest-rate")
    public ResponseEntity<AccountDto> updateAccountInterestRate(
            @PathVariable Long accountId,
            @RequestParam BigDecimal interestRate) {
        AccountDto account = adminService.updateAccountInterestRate(accountId, interestRate);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Delete an account")
    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long accountId) {
        try {
            adminService.deleteAccount(accountId);
            return ResponseEntity.ok("Account deleted successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while deleting the account: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all transactions")
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionDto>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = adminService.getAllTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Get transaction by ID")
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long transactionId) {
        TransactionDto transaction = adminService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }


    @Operation(summary = "Get transactions by account number")
    @GetMapping("/accounts/{accountNumber}/transactions")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByAccountNumber(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = adminService.getTransactionsByAccountNumber(accountNumber, pageable);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Get transactions by customer ID")
    @GetMapping("/customers/{customerId}/transactions")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByCustomerId(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = adminService.getTransactionsByCustomerId(customerId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Get transactions by date range")
    @GetMapping("/transactions/date-range")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = adminService.getTransactionsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Get transactions by status")
    @GetMapping("/transactions/status/{status}")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByStatus(
            @PathVariable Transaction.TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactions = adminService.getTransactionsByStatus(status, pageable);
        return ResponseEntity.ok(transactions);
    }

}
