package com.sam.bankmanagement.Bank.Management.Application.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sam.bankmanagement.Bank.Management.Application.dto.DepositWithdrawRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransferRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.exception.AccountStatusException;
import com.sam.bankmanagement.Bank.Management.Application.exception.InsufficientFundsException;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@DisplayName("Transaction Controller Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private DepositWithdrawRequest validDepositRequest;
    private DepositWithdrawRequest validWithdrawRequest;
    private TransferRequest validTransferRequest;
    private TransactionDto depositTransactionDto;
    private TransactionDto withdrawTransactionDto;
    private TransactionDto transferTransactionDto;
    private List<TransactionDto> transactionDtoList;

    @BeforeEach
    void setUp() {
        validDepositRequest = DepositWithdrawRequest.builder()
                .accountNumber("ACC1234567890")
                .amount(new BigDecimal("500.00"))
                .description("Test deposit")
                .build();

        validWithdrawRequest = DepositWithdrawRequest.builder()
                .accountNumber("ACC1234567890")
                .amount(new BigDecimal("200.00"))
                .description("Test withdrawal")
                .build();

        validTransferRequest = TransferRequest.builder()
                .fromAccountNumber("ACC1234567890")
                .toAccountNumber("ACC0987654321")
                .amount(new BigDecimal("300.00"))
                .description("Test transfer")
                .build();

        depositTransactionDto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN123456DEPOSIT")
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .description("Test deposit")
                .toAccountNumber("ACC1234567890")
                .status(Transaction.TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        withdrawTransactionDto = TransactionDto.builder()
                .id(2L)
                .transactionId("TXN789012WITHDRAW")
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("200.00"))
                .description("Test withdrawal")
                .fromAccountNumber("ACC1234567890")
                .status(Transaction.TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        transferTransactionDto = TransactionDto.builder()
                .id(3L)
                .transactionId("TXN345678TRANSFER")
                .type(Transaction.TransactionType.TRANSFER)
                .amount(new BigDecimal("300.00"))
                .description("Test transfer")
                .fromAccountNumber("ACC1234567890")
                .toAccountNumber("ACC0987654321")
                .status(Transaction.TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        transactionDtoList = Arrays.asList(depositTransactionDto, withdrawTransactionDto, transferTransactionDto);
    }

    @Test
    @DisplayName("Deposit - Success")
    void deposit_Success() throws Exception {
        when(transactionService.deposit(any(DepositWithdrawRequest.class))).thenReturn(depositTransactionDto);

        mockMvc.perform(post("/v1/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDepositRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN123456DEPOSIT"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"));

        verify(transactionService).deposit(any(DepositWithdrawRequest.class));
    }

    @Test
    @DisplayName("Deposit - Inactive Account")
    void deposit_InactiveAccount_ReturnsBadRequest() throws Exception {
        when(transactionService.deposit(any(DepositWithdrawRequest.class)))
                .thenThrow(new AccountStatusException("Account ACC0000000000 is not active. Current status: INACTIVE"));

        mockMvc.perform(post("/v1/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDepositRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account ACC0000000000 is not active. Current status: INACTIVE"));
    }

    @Test
    @DisplayName("Withdraw - Success")
    void withdraw_Success() throws Exception {
        when(transactionService.withdraw(any(DepositWithdrawRequest.class))).thenReturn(withdrawTransactionDto);

        mockMvc.perform(post("/v1/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validWithdrawRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN789012WITHDRAW"))
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"));

        verify(transactionService).withdraw(any(DepositWithdrawRequest.class));
    }

    @Test
    @DisplayName("Withdraw - Insufficient Funds")
    void withdraw_InsufficientFunds_ReturnsBadRequest() throws Exception {
        when(transactionService.withdraw(any(DepositWithdrawRequest.class)))
                .thenThrow(new InsufficientFundsException("Insufficient funds in account ACC1234567890"));

        mockMvc.perform(post("/v1/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validWithdrawRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds in account ACC1234567890"));
    }

    @Test
    @DisplayName("Transfer - Success")
    void transfer_Success() throws Exception {
        when(transactionService.transfer(any(TransferRequest.class))).thenReturn(transferTransactionDto);

        mockMvc.perform(post("/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN345678TRANSFER"))
                .andExpect(jsonPath("$.type").value("TRANSFER"));

        verify(transactionService).transfer(any(TransferRequest.class));
    }

    @Test
    @DisplayName("Transfer - To Account Not Found")
    void transfer_ToAccountNotFound_ReturnsNotFound() throws Exception {
        when(transactionService.transfer(any(TransferRequest.class)))
                .thenThrow(new ResourceNotFoundException("Account", "accountNumber", "ACC0987654321"));

        mockMvc.perform(post("/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found with accountNumber: ACC0987654321"));
    }
}
