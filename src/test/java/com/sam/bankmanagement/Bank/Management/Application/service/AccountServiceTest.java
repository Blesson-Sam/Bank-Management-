package com.sam.bankmanagement.Bank.Management.Application.service;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CreateAccountRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.exception.AccountStatusException;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import com.sam.bankmanagement.Bank.Management.Application.repository.AccountRepository;
import com.sam.bankmanagement.Bank.Management.Application.repository.CustomerRepository;
import com.sam.bankmanagement.Bank.Management.Application.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Service Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private AccountService accountService;

    private Customer activeCustomer;
    private Customer inactiveCustomer;
    private CreateAccountRequest validCreateRequest;
    private Account savingsAccount;
    private Account currentAccount;
    private AccountDto accountDto;
    private List<Account> accountList;

    @BeforeEach
    void setUp() {
        activeCustomer = Customer.builder()
                .id(1L)
                .firstName("blesson")
                .lastName("sam")
                .email("b.sam@email.com")
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        inactiveCustomer = Customer.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@email.com")
                .status(Customer.CustomerStatus.INACTIVE)
                .build();

        validCreateRequest = CreateAccountRequest.builder()
                .customerId(1L)
                .accountType(Account.AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("1000.00"))
                .build();

        savingsAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC1234567890")
                .accountType(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("3.5"))
                .accruedInterest(BigDecimal.ZERO)
                .lastInterestCalculated(LocalDateTime.now())
                .status(Account.AccountStatus.ACTIVE)
                .customer(activeCustomer)
                .build();

        currentAccount = Account.builder()
                .id(2L)
                .accountNumber("ACC0987654321")
                .accountType(Account.AccountType.CURRENT)
                .balance(new BigDecimal("500.00"))
                .interestRate(new BigDecimal("0.5"))
                .accruedInterest(new BigDecimal("25.50"))
                .status(Account.AccountStatus.ACTIVE)
                .customer(activeCustomer)
                .build();

        accountDto = AccountDto.builder()
                .id(1L)
                .accountNumber("ACC1234567890")
                .accountType(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("3.5"))
                .status(Account.AccountStatus.ACTIVE)
                .customerId(1L)
                .customerName("John Doe")
                .build();

        accountList = Arrays.asList(savingsAccount, currentAccount);
    }

    @Test
    @DisplayName("Create Account - Success with Initial Deposit")
    void createAccount_SuccessWithInitialDeposit() {

        when(customerRepository.findById(1L)).thenReturn(Optional.of(activeCustomer));
        when(accountRepository.save(any(Account.class))).thenReturn(savingsAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(dtoMapper.toAccountDto(any(Account.class))).thenReturn(accountDto);

        AccountDto result = accountService.createAccount(validCreateRequest);

        assertNotNull(result);
        assertEquals("ACC1234567890", result.getAccountNumber());
        assertEquals(Account.AccountType.SAVINGS, result.getAccountType());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        assertEquals(1L, result.getCustomerId());

        verify(customerRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class)); // Initial deposit transaction
        verify(dtoMapper).toAccountDto(any(Account.class));
    }


    @Test
    @DisplayName("Create Account - Inactive Customer")
    void createAccount_InactiveCustomer_ThrowsException() {

        when(customerRepository.findById(2L)).thenReturn(Optional.of(inactiveCustomer));

        CreateAccountRequest request = CreateAccountRequest.builder()
                .customerId(2L)
                .accountType(Account.AccountType.SAVINGS)
                .build();

        AccountStatusException exception = assertThrows(AccountStatusException.class,
                () -> accountService.createAccount(request));

        assertEquals("Cannot create account for inactive customer", exception.getMessage());
        verify(customerRepository).findById(2L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Get Account By Number - Success")
    void getAccountByNumber_Success() {
        // Given
        when(accountRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(savingsAccount));
        when(dtoMapper.toAccountDto(savingsAccount)).thenReturn(accountDto);

        AccountDto result = accountService.getAccountByNumber("ACC1234567890");

        assertNotNull(result);
        assertEquals("ACC1234567890", result.getAccountNumber());
        verify(accountRepository).findByAccountNumber("ACC1234567890");
        verify(dtoMapper).toAccountDto(savingsAccount);
    }



    @Test
    @DisplayName("Get Accounts By Customer ID - Success")
    void getAccountsByCustomerId_Success() {

        when(accountRepository.findByCustomerId(1L)).thenReturn(accountList);
        when(dtoMapper.toAccountDtos(accountList)).thenReturn(Arrays.asList(accountDto));

        List<AccountDto> result = accountService.getAccountsByCustomerId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository).findByCustomerId(1L);
        verify(dtoMapper).toAccountDtos(accountList);
    }

    @Test
    @DisplayName("Get All Accounts - Success")
    void getAllAccounts_Success() {

        when(accountRepository.findAll()).thenReturn(accountList);
        when(dtoMapper.toAccountDtos(accountList)).thenReturn(Arrays.asList(accountDto));

        List<AccountDto> result = accountService.getAllAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository).findAll();
        verify(dtoMapper).toAccountDtos(accountList);
    }

    @Test
    @DisplayName("Update Account Status - Success")
    void updateAccountStatus_Success() {

        when(accountRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(savingsAccount);
        when(dtoMapper.toAccountDto(any(Account.class))).thenReturn(accountDto);

        AccountDto result = accountService.updateAccountStatus("ACC1234567890", Account.AccountStatus.FROZEN);

        assertNotNull(result);
        verify(accountRepository).findByAccountNumber("ACC1234567890");
        verify(accountRepository).save(any(Account.class));
        verify(dtoMapper).toAccountDto(any(Account.class));
    }

    @Test
    @DisplayName("Update Interest Rate - Success")
    void updateInterestRate_Success() {

        BigDecimal newRate = new BigDecimal("4.0");
        when(accountRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(savingsAccount);
        when(dtoMapper.toAccountDto(any(Account.class))).thenReturn(accountDto);

        AccountDto result = accountService.updateInterestRate("ACC1234567890", newRate);

        assertNotNull(result);
        verify(accountRepository).findByAccountNumber("ACC1234567890");
        verify(accountRepository).save(any(Account.class));
        verify(dtoMapper).toAccountDto(any(Account.class));
    }


    @Test
    @DisplayName("Calculate Daily Interest - Success")
    void calculateDailyInterest_Success() {

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1);
        when(accountRepository.findAccountsForInterestCalculation(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(savingsAccount, currentAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(savingsAccount);


        assertDoesNotThrow(() -> accountService.calculateDailyInterest());

        verify(accountRepository).findAccountsForInterestCalculation(any(LocalDateTime.class));
        verify(accountRepository, times(2)).save(any(Account.class)); // Both accounts updated
    }

}
