package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.CreateCustomerRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.service.CustomerService;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Controller Tests")
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private CustomerController customerController;

    private CreateCustomerRequest validCreateRequest;
    private CustomerDto validCustomerDto;
    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        validCreateRequest = CreateCustomerRequest.builder()
                .firstName("Blesson")
                .lastName("Sam")
                .email("b.sam@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, State")
                .nationalId("ID123456789")
                .build();

        validCustomerDto = CustomerDto.builder()
                .id(1L)
                .firstName("Blesson")
                .lastName("Sam")
                .email("b.sam@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, State")
                .nationalId("ID123456789")
                .status(Customer.CustomerStatus.ACTIVE)
                .gender("MALE") // Added gender field
                .createdAt(LocalDateTime.now())
                .build();

        mockCustomer = Customer.builder()
                .id(1L)
                .firstName("Blesson")
                .lastName("Sam")
                .email("b.sam@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, State")
                .nationalId("ID123456789")
                .status(Customer.CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get customer by ID successfully when accessing own profile")
    void getCustomerById_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentCustomerId).thenReturn(1L);

            when(customerService.getCustomerById(anyLong()))
                    .thenReturn(validCustomerDto);

            ResponseEntity<CustomerDto> response = customerController.getCustomerById(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getId());
            assertEquals("Blesson", response.getBody().getFirstName());
            assertEquals("b.sam@email.com", response.getBody().getEmail());

            verify(customerService).getCustomerById(1L);
        }
    }

    @Test
    @DisplayName("Should return forbidden when accessing other customer's profile")
    void getCustomerById_Forbidden() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentCustomerId).thenReturn(2L);

            ResponseEntity<CustomerDto> response = customerController.getCustomerById(1L);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());

            verify(customerService, never()).getCustomerById(any());
        }
    }



    @Test
    @DisplayName("Should update customer successfully when updating own profile")
    void updateCustomer_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentCustomerId).thenReturn(1L);

            CustomerDto updatedCustomerDto = CustomerDto.builder()
                    .id(1L)
                    .firstName("Blesson")
                    .lastName("Samuel")
                    .email("b.sam@email.com")
                    .phone("+1234567890")
                    .address("123 Main St, City, State")
                    .nationalId("ID123456789")
                    .status(Customer.CustomerStatus.ACTIVE)
                    .gender("MALE") // Added gender field
                    .createdAt(LocalDateTime.now())
                    .build();

            when(customerService.updateCustomer(eq(1L), any(CreateCustomerRequest.class)))
                    .thenReturn(updatedCustomerDto);

            ResponseEntity<CustomerDto> response = customerController.updateCustomer(1L, validCreateRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Samuel", response.getBody().getLastName());

            verify(customerService).updateCustomer(eq(1L), any(CreateCustomerRequest.class));
        }
    }

    @Test
    @DisplayName("Should get current customer profile successfully")
    void getCurrentCustomerProfile_Success() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentCustomer).thenReturn(mockCustomer);

            when(dtoMapper.toCustomerDto(any(Customer.class)))
                    .thenReturn(validCustomerDto);

            ResponseEntity<CustomerDto> response = customerController.getCurrentCustomerProfile();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Blesson", response.getBody().getFirstName());
            assertEquals("b.sam@email.com", response.getBody().getEmail());

            verify(dtoMapper).toCustomerDto(mockCustomer);
        }
    }
}
