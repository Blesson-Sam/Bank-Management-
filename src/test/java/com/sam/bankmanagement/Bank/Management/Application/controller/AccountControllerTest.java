package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CreateAccountRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.service.AccountService;
import com.sam.bankmanagement.Bank.Management.Application.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Controller Tests")
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private CreateAccountRequest validCreateRequest;
    private AccountDto savingsAccountDto;
    private List<AccountDto> accountDtoList;

    @BeforeEach
    void setUp() {
        validCreateRequest = CreateAccountRequest.builder()
                .customerId(1L)
                .accountType(Account.AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("1000.00"))
                .build();

        savingsAccountDto = AccountDto.builder()
                .id(1L)
                .accountNumber("ACC123456")
                .accountType(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("3.5"))
                .status(Account.AccountStatus.ACTIVE)
                .customerId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        AccountDto currentAccountDto = AccountDto.builder()
                .id(2L)
                .accountNumber("ACC789012")
                .accountType(Account.AccountType.CURRENT)
                .balance(new BigDecimal("2000.00"))
                .interestRate(new BigDecimal("0.5"))
                .status(Account.AccountStatus.ACTIVE)
                .customerId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        accountDtoList = Arrays.asList(savingsAccountDto, currentAccountDto);
    }

    @Test
    @DisplayName("Should create account successfully")
    void createAccount_Success() throws Exception {
        // Mock SecurityUtil to return a customer ID
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentCustomerId).thenReturn(1L);

            when(accountService.createAccount(any(CreateAccountRequest.class)))
                    .thenReturn(savingsAccountDto);

            ResponseEntity<AccountDto> response = accountController.createAccount(validCreateRequest);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("ACC123456", response.getBody().getAccountNumber());
            assertEquals(Account.AccountType.SAVINGS, response.getBody().getAccountType());
            assertEquals(new BigDecimal("1000.00"), response.getBody().getBalance());

            verify(accountService).createAccount(any(CreateAccountRequest.class));
        }
    }

    @Test
    @DisplayName("Should get my accounts successfully")
    void getMyAccounts_Success() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentCustomerId).thenReturn(1L);

            when(accountService.getAccountsByCustomerId(anyLong()))
                    .thenReturn(accountDtoList);

            ResponseEntity<List<AccountDto>> response = accountController.getMyAccounts();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals("ACC123456", response.getBody().get(0).getAccountNumber());
            assertEquals("ACC789012", response.getBody().get(1).getAccountNumber());

            verify(accountService).getAccountsByCustomerId(1L);
        }
    }

    @Test
    @DisplayName("Should get account by number successfully")
    void getAccountByNumber_Success() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentCustomerId).thenReturn(1L);

            when(accountService.getAccountByNumber(anyString()))
                    .thenReturn(savingsAccountDto);

            ResponseEntity<AccountDto> response = accountController.getAccountByNumber("ACC123456");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("ACC123456", response.getBody().getAccountNumber());
            assertEquals(Account.AccountType.SAVINGS, response.getBody().getAccountType());

            verify(accountService).getAccountByNumber("ACC123456");
        }
    }

    @Test
    @DisplayName("Should return forbidden when accessing other customer's account")
    void getAccountByNumber_Forbidden() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentCustomerId).thenReturn(2L); // Different customer ID

            AccountDto otherCustomerAccount = AccountDto.builder()
                    .id(1L)
                    .accountNumber("ACC123456")
                    .accountType(Account.AccountType.SAVINGS)
                    .balance(new BigDecimal("1000.00"))
                    .interestRate(new BigDecimal("3.5"))
                    .status(Account.AccountStatus.ACTIVE)
                    .customerId(1L) // Different customer
                    .createdAt(LocalDateTime.now())
                    .build();

            when(accountService.getAccountByNumber(anyString()))
                    .thenReturn(otherCustomerAccount);

            ResponseEntity<AccountDto> response = accountController.getAccountByNumber("ACC123456");

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());

            verify(accountService).getAccountByNumber("ACC123456");
        }
    }

    @Test
    @DisplayName("Should update account status successfully")
    void updateAccountStatus_Success() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentCustomerId).thenReturn(1L);

            AccountDto updatedAccount = AccountDto.builder()
                    .id(1L)
                    .accountNumber("ACC123456")
                    .accountType(Account.AccountType.SAVINGS)
                    .balance(new BigDecimal("1000.00"))
                    .interestRate(new BigDecimal("3.5"))
                    .status(Account.AccountStatus.CLOSED)
                    .customerId(1L)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(accountService.getAccountByNumber(anyString()))
                    .thenReturn(savingsAccountDto); // First call to check ownership
            when(accountService.updateAccountStatus(anyString(), any(Account.AccountStatus.class)))
                    .thenReturn(updatedAccount);

            ResponseEntity<AccountDto> response = accountController.updateAccountStatus("ACC123456", Account.AccountStatus.CLOSED);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(Account.AccountStatus.CLOSED, response.getBody().getStatus());

            verify(accountService).getAccountByNumber("ACC123456");
            verify(accountService).updateAccountStatus("ACC123456", Account.AccountStatus.CLOSED);
        }
    }
}
