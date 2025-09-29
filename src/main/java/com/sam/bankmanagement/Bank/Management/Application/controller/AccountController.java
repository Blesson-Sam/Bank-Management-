package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CreateAccountRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.service.AccountService;
import com.sam.bankmanagement.Bank.Management.Application.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("accounts")
    @Operation(summary = "Create a new account", description = "Creates a new savings or current account for the authenticated customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<AccountDto> createAccount(
             @RequestBody CreateAccountRequest request) {
        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();
        request.setCustomerId(currentCustomerId);

        AccountDto accountDto = accountService.createAccount(request);
        return new ResponseEntity<>(accountDto, HttpStatus.CREATED);
    }

    @GetMapping("/my-accounts")
    @Operation(summary = "Get my accounts", description = "Retrieves all accounts for the authenticated customer")
    @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    public ResponseEntity<List<AccountDto>> getMyAccounts() {
        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();
        List<AccountDto> accounts = accountService.getAccountsByCustomerId(currentCustomerId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get my account by number", description = "Retrieves account details by account number (only if owned by authenticated customer)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto> getAccountByNumber(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {
        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();
        AccountDto accountDto = accountService.getAccountByNumber(accountNumber);

        if (!accountDto.getCustomerId().equals(currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(accountDto);
    }

    @PatchMapping("/{accountNumber}/status")
    @Operation(summary = "Update my account status", description = "Updates account status (only for owned accounts)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto> updateAccountStatus(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber,
            @Parameter(description = "New status", required = true)
            @RequestParam Account.AccountStatus status) {

        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();
        AccountDto accountDto = accountService.getAccountByNumber(accountNumber);

        if (!accountDto.getCustomerId().equals(currentCustomerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AccountDto updatedAccount = accountService.updateAccountStatus(accountNumber, status);
        return ResponseEntity.ok(updatedAccount);
    }
}
