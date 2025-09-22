package com.sam.bankmanagement.Bank.Management.Application.service;

import com.sam.bankmanagement.Bank.Management.Application.dto.DepositWithdrawRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransferRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.exception.AccountStatusException;
import com.sam.bankmanagement.Bank.Management.Application.exception.InsufficientFundsException;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import com.sam.bankmanagement.Bank.Management.Application.repository.AccountRepository;
import com.sam.bankmanagement.Bank.Management.Application.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Service Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Account activeAccount;
    private Account inactiveAccount;
    private Account toAccount;
    private Transaction completedTransaction;
    private TransactionDto transactionDto;
    private DepositWithdrawRequest depositRequest;
    private DepositWithdrawRequest withdrawRequest;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder()
                .id(1L)
                .firstName("Blesson")
                .lastName("Sam")
                .build();

        activeAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC1234567890")
                .accountType(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        inactiveAccount = Account.builder()
                .id(2L)
                .accountNumber("ACC0000000000")
                .status(Account.AccountStatus.INACTIVE)
                .build();

        toAccount = Account.builder()
                .id(3L)
                .accountNumber("ACC0987654321")
                .balance(new BigDecimal("1000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        completedTransaction = Transaction.builder()
                .id(1L)
                .transactionId("TXN123456")
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .description("Test deposit")
                .toAccount(activeAccount)
                .status(Transaction.TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        transactionDto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN123456")
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .description("Test deposit")
                .toAccountNumber("ACC1234567890")
                .status(Transaction.TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        depositRequest = DepositWithdrawRequest.builder()
                .accountNumber("ACC1234567890")
                .amount(new BigDecimal("500.00"))
                .description("Test deposit")
                .build();

        withdrawRequest = DepositWithdrawRequest.builder()
                .accountNumber("ACC1234567890")
                .amount(new BigDecimal("200.00"))
                .description("Test withdrawal")
                .build();

        transferRequest = TransferRequest.builder()
                .fromAccountNumber("ACC1234567890")
                .toAccountNumber("ACC0987654321")
                .amount(new BigDecimal("300.00"))
                .description("Test transfer")
                .build();
    }

    @Test
    @DisplayName("Deposit - Success")
    void deposit_Success() {

        when(accountRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(activeAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(completedTransaction);
        when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);
        when(dtoMapper.toTransactionDto(any(Transaction.class))).thenReturn(transactionDto);

        TransactionDto result = transactionService.deposit(depositRequest);

        assertNotNull(result);
        assertEquals("TXN123456", result.getTransactionId());
        assertEquals(Transaction.TransactionType.DEPOSIT, result.getType());
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());

        verify(accountRepository).findByAccountNumber("ACC1234567890");
        verify(transactionRepository, times(2)).save(any(Transaction.class)); // Pending then completed
        verify(accountRepository).save(any(Account.class));
        verify(dtoMapper).toTransactionDto(any(Transaction.class));
    }


    @Test
    @DisplayName("Deposit - Inactive Account")
    void deposit_InactiveAccount_ThrowsException() {

        when(accountRepository.findByAccountNumber("ACC0000000000")).thenReturn(Optional.of(inactiveAccount));

        DepositWithdrawRequest inactiveAccountRequest = DepositWithdrawRequest.builder()
                .accountNumber("ACC0000000000")
                .amount(new BigDecimal("500.00"))
                .build();

        AccountStatusException exception = assertThrows(AccountStatusException.class,
                () -> transactionService.deposit(inactiveAccountRequest));

        assertTrue(exception.getMessage().contains("is not active"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Withdraw - Success")
    void withdraw_Success() {

        when(accountRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(activeAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(completedTransaction);
        when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);
        when(dtoMapper.toTransactionDto(any(Transaction.class))).thenReturn(transactionDto);

        TransactionDto result = transactionService.withdraw(withdrawRequest);

        assertNotNull(result);
        assertEquals(Transaction.TransactionType.DEPOSIT, result.getType());
        verify(accountRepository).findByAccountNumber("ACC1234567890");
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Withdraw - Insufficient Funds")
    void withdraw_InsufficientFunds_ThrowsException() {

        Account lowBalanceAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC1234567890")
                .balance(new BigDecimal("100.00"))
                .status(Account.AccountStatus.ACTIVE)
                .build();

        DepositWithdrawRequest largeWithdrawRequest = DepositWithdrawRequest.builder()
                .accountNumber("ACC1234567890")
                .amount(new BigDecimal("500.00"))
                .build();

        when(accountRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(lowBalanceAccount));

        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> transactionService.withdraw(largeWithdrawRequest));

        assertTrue(exception.getMessage().contains("Insufficient funds"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }


    @Test
    @DisplayName("Transfer - Success")
    void transfer_Success() {

        when(accountRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByAccountNumber("ACC0987654321")).thenReturn(Optional.of(toAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(completedTransaction);
        when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);
        when(dtoMapper.toTransactionDto(any(Transaction.class))).thenReturn(transactionDto);

        TransactionDto result = transactionService.transfer(transferRequest);

        assertNotNull(result);
        verify(accountRepository).findByAccountNumber("ACC1234567890");
        verify(accountRepository).findByAccountNumber("ACC0987654321");
        verify(transactionRepository, times(2)).save(Mockito.any());

    }

    @Test
    @DisplayName("Transfer - Same Account")
    void transfer_SameAccount_ThrowsException() {

        TransferRequest sameAccountRequest = TransferRequest.builder()
                .fromAccountNumber("ACC1234567890")
                .toAccountNumber("ACC1234567890")
                .amount(new BigDecimal("100.00"))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(sameAccountRequest));

        assertEquals("Cannot transfer to the same account", exception.getMessage());
        verify(accountRepository, never()).findByAccountNumber(anyString());
    }



    @Test
    @DisplayName("Get Transaction By ID - Success")
    void getTransactionById_Success() {

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(completedTransaction));
        when(dtoMapper.toTransactionDto(completedTransaction)).thenReturn(transactionDto);

        TransactionDto result = transactionService.getTransactionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(transactionRepository).findById(1L);
        verify(dtoMapper).toTransactionDto(completedTransaction);
    }


    @Test
    @DisplayName("Get Transactions By Account ID - Success")
    void getTransactionsByAccountId_Success() {

        Page<Transaction> transactionPage = new PageImpl<>(Arrays.asList(completedTransaction));
        when(transactionRepository.findByAccountId(eq(1L), any(Pageable.class))).thenReturn(transactionPage);
        when(dtoMapper.toTransactionDto(any(Transaction.class))).thenReturn(transactionDto);

        Page<TransactionDto> result = transactionService.getTransactionsByAccountId(1L, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findByAccountId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Get Transactions By Account Number - Success")
    void getTransactionsByAccountNumber_Success() {

        when(accountRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(activeAccount));
        when(transactionRepository.findByAccountNumber("ACC1234567890")).thenReturn(Arrays.asList(completedTransaction));
        when(dtoMapper.toTransactionDtos(any())).thenReturn(Arrays.asList(transactionDto));

        List<TransactionDto> result = transactionService.getTransactionsByAccountNumber("ACC1234567890");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository).findByAccountNumber("ACC1234567890");
        verify(transactionRepository).findByAccountNumber("ACC1234567890");
    }

    @Test
    @DisplayName("Get Transactions By Account And Date Range - Success")
    void getTransactionsByAccountAndDateRange_Success() {

        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(accountRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(activeAccount));
        when(transactionRepository.findByAccountIdAndDateRange(eq(1L), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList(completedTransaction));
        when(dtoMapper.toTransactionDtos(any())).thenReturn(Arrays.asList(transactionDto));

        List<TransactionDto> result = transactionService.getTransactionsByAccountAndDateRange(
                "ACC1234567890", startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository).findByAccountNumber("ACC1234567890");
        verify(transactionRepository).findByAccountIdAndDateRange(1L, startDate, endDate);
    }

    @Test
    @DisplayName("Get All Transactions - Success")
    void getAllTransactions_Success() {

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(completedTransaction));
        when(dtoMapper.toTransactionDtos(any())).thenReturn(Arrays.asList(transactionDto));

        List<TransactionDto> result = transactionService.getAllTransactions();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findAll();
        verify(dtoMapper).toTransactionDtos(any());
    }
}
