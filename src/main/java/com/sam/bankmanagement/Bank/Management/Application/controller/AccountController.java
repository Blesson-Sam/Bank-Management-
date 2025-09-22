package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CreateAccountRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account", description = "Creates a new savings or current account for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<AccountDto> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountDto accountDto = accountService.createAccount(request);
        return new ResponseEntity<>(accountDto, HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account by number", description = "Retrieves account details by account number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto> getAccountByNumber(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {
        AccountDto accountDto = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(accountDto);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get accounts by customer ID", description = "Retrieves all accounts for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<List<AccountDto>> getAccountsByCustomerId(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long customerId) {
        List<AccountDto> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping
    @Operation(summary = "Get all accounts", description = "Retrieves all accounts in the system")
    @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        List<AccountDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @PatchMapping("/{accountNumber}/status")
    @Operation(summary = "Update account status", description = "Updates account status (ACTIVE, INACTIVE, CLOSED, FROZEN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto> updateAccountStatus(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            @Parameter(description = "New status", required = true)
            @RequestParam Account.AccountStatus status) {
        AccountDto accountDto = accountService.updateAccountStatus(accountNumber, status);
        return ResponseEntity.ok(accountDto);
    }

    @PatchMapping("/{accountNumber}/interest-rate")
    @Operation(summary = "Update interest rate", description = "Updates the interest rate for an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest rate updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto> updateInterestRate(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            @Parameter(description = "New interest rate", required = true)
            @RequestParam BigDecimal interestRate) {
        AccountDto accountDto = accountService.updateInterestRate(accountNumber, interestRate);
        return ResponseEntity.ok(accountDto);
    }

    @PostMapping("/{accountNumber}/credit-interest")
    @Operation(summary = "Credit accrued interest", description = "Credits the accrued interest to account balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest credited successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<String> creditAccruedInterest(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {
        accountService.creditAccruedInterest(accountNumber);
        return ResponseEntity.ok("Accrued interest credited successfully");
    }
}
