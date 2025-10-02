package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin Controller Tests")
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private CustomerDto customerDto;
    private AccountDto accountDto;
    private TransactionDto transactionDto;
    private List<CustomerDto> customerList;
    private List<AccountDto> accountList;
    private List<TransactionDto> transactionList;

    @BeforeEach
    void setUp() {
        customerDto = CustomerDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@email.com")
                .phone("+1234567890")
                .address("123 Main St")
                .nationalId("ID123456789")
                .status(Customer.CustomerStatus.ACTIVE)
                .gender("MALE")
                .createdAt(LocalDateTime.now())
                .build();

        accountDto = AccountDto.builder()
                .id(1L)
                .accountNumber("ACC123456")
                .accountType(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("3.5"))
                .status(Account.AccountStatus.ACTIVE)
                .customerId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        transactionDto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN123456")
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .description("Test deposit")
                .toAccountNumber("ACC123456")
                .status(Transaction.TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        customerList = Collections.singletonList(customerDto);
        accountList = Collections.singletonList(accountDto);
        transactionList = Collections.singletonList(transactionDto);
    }

    @Test
    @DisplayName("Should get all customers with pagination")
    void getAllCustomers_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerDto> customerPage = new PageImpl<>(customerList, pageable, customerList.size());

        when(adminService.getAllCustomers(any(Pageable.class)))
                .thenReturn(customerPage);

        ResponseEntity<Page<CustomerDto>> response = adminController.getAllCustomers(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("John", response.getBody().getContent().get(0).getFirstName());
        assertEquals("john.doe@email.com", response.getBody().getContent().get(0).getEmail());

        verify(adminService).getAllCustomers(any(Pageable.class));
    }

    @Test
    @DisplayName("Should update customer status")
    void updateCustomerStatus_Success() throws Exception {
        when(adminService.updateCustomerStatus(anyLong(), any(Customer.CustomerStatus.class)))
                .thenReturn(customerDto);

        ResponseEntity<CustomerDto> response = adminController.updateCustomerStatus(1L, Customer.CustomerStatus.SUSPENDED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John", response.getBody().getFirstName());

        verify(adminService).updateCustomerStatus(1L, Customer.CustomerStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void deleteCustomer_Success() throws Exception {
        doNothing().when(adminService).deleteCustomer(anyLong());

        ResponseEntity<?> response = adminController.deleteCustomer(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Customer deleted successfully", response.getBody());

        verify(adminService).deleteCustomer(1L);
    }

    @Test
    @DisplayName("Should handle delete customer with active accounts")
    void deleteCustomer_WithActiveAccounts() throws Exception {
        doThrow(new IllegalStateException("Cannot delete customer with active accounts. Please close all accounts first."))
                .when(adminService).deleteCustomer(anyLong());

        ResponseEntity<?> response = adminController.deleteCustomer(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cannot delete customer with active accounts. Please close all accounts first.", response.getBody());

        verify(adminService).deleteCustomer(1L);
    }

    @Test
    @DisplayName("Should get all accounts with pagination")
    void getAllAccounts_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountDto> accountPage = new PageImpl<>(accountList, pageable, accountList.size());

        when(adminService.getAllAccounts(any(Pageable.class)))
                .thenReturn(accountPage);

        ResponseEntity<Page<AccountDto>> response = adminController.getAllAccounts(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("ACC123456", response.getBody().getContent().get(0).getAccountNumber());

        verify(adminService).getAllAccounts(any(Pageable.class));
    }


    @Test
    @DisplayName("Should update account status")
    void updateAccountStatus_Success() throws Exception {
        when(adminService.updateAccountStatus(anyLong(), any(Account.AccountStatus.class)))
                .thenReturn(accountDto);

        ResponseEntity<AccountDto> response = adminController.updateAccountStatus(1L, Account.AccountStatus.FROZEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("ACC123456", response.getBody().getAccountNumber());

        verify(adminService).updateAccountStatus(1L, Account.AccountStatus.FROZEN);
    }

    @Test
    @DisplayName("Should update account interest rate")
    void updateAccountInterestRate_Success() throws Exception {
        BigDecimal newRate = new BigDecimal("4.0");
        when(adminService.updateAccountInterestRate(anyLong(), any(BigDecimal.class)))
                .thenReturn(accountDto);

        ResponseEntity<AccountDto> response = adminController.updateAccountInterestRate(1L, newRate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(adminService).updateAccountInterestRate(1L, newRate);
    }

    @Test
    @DisplayName("Should delete account successfully")
    void deleteAccount_Success() throws Exception {
        doNothing().when(adminService).deleteAccount(anyLong());

        ResponseEntity<?> response = adminController.deleteAccount(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account deleted successfully", response.getBody());

        verify(adminService).deleteAccount(1L);
    }

    @Test
    @DisplayName("Should get all transactions with pagination")
    void getAllTransactions_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 100);
        Page<TransactionDto> transactionPage = new PageImpl<>(transactionList, pageable, transactionList.size());

        when(adminService.getAllTransactions(any(Pageable.class)))
                .thenReturn(transactionPage);

        ResponseEntity<Page<TransactionDto>> response = adminController.getAllTransactions(0, 100);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("TXN123456", response.getBody().getContent().get(0).getTransactionId());

        verify(adminService).getAllTransactions(any(Pageable.class));
    }



    @Test
    @DisplayName("Should get transactions by date range with pagination")
    void getTransactionsByDateRange_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TransactionDto> transactionPage = new PageImpl<>(transactionList, pageable, transactionList.size());

        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(adminService.getTransactionsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(transactionPage);

        ResponseEntity<Page<TransactionDto>> response = adminController.getTransactionsByDateRange(startDate, endDate, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        verify(adminService).getTransactionsByDateRange(eq(startDate), eq(endDate), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get transactions by status with pagination")
    void getTransactionsByStatus_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 100);
        Page<TransactionDto> transactionPage = new PageImpl<>(transactionList, pageable, transactionList.size());

        when(adminService.getTransactionsByStatus(any(Transaction.TransactionStatus.class), any(Pageable.class)))
                .thenReturn(transactionPage);

        ResponseEntity<Page<TransactionDto>> response = adminController.getTransactionsByStatus(Transaction.TransactionStatus.COMPLETED, 0, 100);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        verify(adminService).getTransactionsByStatus(eq(Transaction.TransactionStatus.COMPLETED), any(Pageable.class));
    }
}
