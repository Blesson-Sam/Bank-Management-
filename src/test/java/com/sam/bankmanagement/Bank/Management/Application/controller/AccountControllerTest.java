package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CreateAccountRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.exception.AccountStatusException;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@DisplayName("Account Controller Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateAccountRequest validCreateRequest;
    private AccountDto savingsAccountDto;
    private AccountDto currentAccountDto;
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
                .accountNumber("ACC1234567890")
                .accountType(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("3.5"))
                .accruedInterest(BigDecimal.ZERO)
                .status(Account.AccountStatus.ACTIVE)
                .customerId(1L)
                .customerName("Blesson Sam")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        currentAccountDto = AccountDto.builder()
                .id(2L)
                .accountNumber("ACC0987654321")
                .accountType(Account.AccountType.CURRENT)
                .balance(new BigDecimal("500.00"))
                .interestRate(new BigDecimal("0.5"))
                .accruedInterest(new BigDecimal("25.50"))
                .status(Account.AccountStatus.ACTIVE)
                .customerId(1L)
                .customerName("Blesson Sam")
                .build();

        accountDtoList = Arrays.asList(savingsAccountDto, currentAccountDto);
    }

    @Test
    @DisplayName("Create Account - Success")
    void createAccount_Success() throws Exception {

        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(savingsAccountDto);
        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("ACC1234567890"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.interestRate").value(3.5))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.customerName").value("Blesson Sam"));

        verify(accountService).createAccount(any(CreateAccountRequest.class));
    }



    @Test
    @DisplayName("Create Account - Inactive Customer")
    void createAccount_InactiveCustomer_ReturnsBadRequest() throws Exception {

        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenThrow(new AccountStatusException("Cannot create account for inactive customer"));

        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot create account for inactive customer"));

        verify(accountService).createAccount(any(CreateAccountRequest.class));
    }

    @Test
    @DisplayName("Get Account By Number - Success")
    void getAccountByNumber_Success() throws Exception {

        when(accountService.getAccountByNumber("ACC1234567890")).thenReturn(savingsAccountDto);

        mockMvc.perform(get("/v1/accounts/ACC1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC1234567890"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.customerName").value("Blesson Sam"));

        verify(accountService).getAccountByNumber("ACC1234567890");
    }

    @Test
    @DisplayName("Get Accounts By Customer ID - Success")
    void getAccountsByCustomerId_Success() throws Exception {

        when(accountService.getAccountsByCustomerId(1L)).thenReturn(accountDtoList);

        mockMvc.perform(get("/v1/accounts/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountType").value("SAVINGS"))
                .andExpect(jsonPath("$[1].accountType").value("CURRENT"));

        verify(accountService).getAccountsByCustomerId(1L);
    }

    @Test
    @DisplayName("Get All Accounts - Success")
    void getAllAccounts_Success() throws Exception {

        when(accountService.getAllAccounts()).thenReturn(accountDtoList);

        mockMvc.perform(get("/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(accountService).getAllAccounts();
    }

    @Test
    @DisplayName("Update Account Status - Success")
    void updateAccountStatus_Success() throws Exception {

        AccountDto frozenAccountDto = AccountDto.builder()
                .id(1L)
                .accountNumber("ACC1234567890")
                .status(Account.AccountStatus.FROZEN)
                .build();

        when(accountService.updateAccountStatus("ACC1234567890", Account.AccountStatus.FROZEN))
                .thenReturn(frozenAccountDto);

        mockMvc.perform(patch("/v1/accounts/ACC1234567890/status")
                        .param("status", "FROZEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));

        verify(accountService).updateAccountStatus("ACC1234567890", Account.AccountStatus.FROZEN);
    }

    @Test
    @DisplayName("Update Interest Rate - Success")
    void updateInterestRate_Success() throws Exception {

        BigDecimal newRate = new BigDecimal("4.0");
        AccountDto updatedAccountDto = AccountDto.builder()
                .id(1L)
                .accountNumber("ACC1234567890")
                .interestRate(newRate)
                .build();

        when(accountService.updateInterestRate("ACC1234567890", newRate))
                .thenReturn(updatedAccountDto);

        mockMvc.perform(patch("/v1/accounts/ACC1234567890/interest-rate")
                        .param("interestRate", "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interestRate").value(4.0));

        verify(accountService).updateInterestRate("ACC1234567890", newRate);
    }


    @Test
    @DisplayName("Credit Accrued Interest - Success")
    void creditAccruedInterest_Success() throws Exception {
        // Given
        doNothing().when(accountService).creditAccruedInterest("ACC1234567890");

        // When & Then
        mockMvc.perform(post("/v1/accounts/ACC1234567890/credit-interest"))
                .andExpect(status().isOk())
                .andExpect(content().string("Accrued interest credited successfully"));

        verify(accountService).creditAccruedInterest("ACC1234567890");
    }

}
