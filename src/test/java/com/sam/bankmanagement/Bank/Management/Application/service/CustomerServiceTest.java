package com.sam.bankmanagement.Bank.Management.Application.service;

import com.sam.bankmanagement.Bank.Management.Application.dto.CreateCustomerRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.exception.DuplicateResourceException;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import com.sam.bankmanagement.Bank.Management.Application.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private CustomerService customerService;

    private CreateCustomerRequest validCreateRequest;
    private Customer validCustomer;
    private CustomerDto validCustomerDto;
    private List<Customer> customerList;
    private List<CustomerDto> customerDtoList;

    @BeforeEach
    void setUp() {
        validCreateRequest = CreateCustomerRequest.builder()
                .firstName("blesson")
                .lastName("sam")
                .email("b.sam@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, Country")
                .nationalId("ID123456789")
                .build();

        validCustomer = Customer.builder()
                .id(1L)
                .firstName("blesson")
                .lastName("sam")
                .email("b.sam@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, Country")
                .nationalId("ID123456789")
                .status(Customer.CustomerStatus.ACTIVE)
                .accounts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validCustomerDto = CustomerDto.builder()
                .id(1L)
                .firstName("blesson")
                .lastName("sam")
                .email("b.sam@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, Country")
                .nationalId("ID123456789")
                .status(Customer.CustomerStatus.ACTIVE)
                .accounts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Customer customer2 = Customer.builder()
                .id(2L)
                .firstName("David")
                .lastName("sam")
                .email("david.sam@email.com")
                .phone("+0987654321")
                .address("456 Oak Ave, City, Country")
                .nationalId("ID987654321")
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        customerList = Arrays.asList(validCustomer, customer2);
        customerDtoList = Arrays.asList(validCustomerDto,
                CustomerDto.builder().id(2L).firstName("Jane").lastName("Smith").build());
    }

    @Test
    @DisplayName("Create Customer - Success")
    void createCustomer_Success() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByNationalId(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(validCustomer);
        when(dtoMapper.toCustomerDto(any(Customer.class))).thenReturn(validCustomerDto);

        // When
        CustomerDto result = customerService.createCustomer(validCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("blesson", result.getFirstName());
        assertEquals("sam", result.getLastName());
        assertEquals("b.sam@email.com", result.getEmail());
        assertEquals(Customer.CustomerStatus.ACTIVE, result.getStatus());

        verify(customerRepository).existsByEmail("b.sam@email.com");
        verify(customerRepository).existsByNationalId("ID123456789");
        verify(customerRepository).save(any(Customer.class));
        verify(dtoMapper).toCustomerDto(any(Customer.class));
    }

    @Test
    @DisplayName("Create Customer - Duplicate Email")
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> customerService.createCustomer(validCreateRequest));

        assertEquals("Customer with email b.sam@email.com already exists", exception.getMessage());
        verify(customerRepository).existsByEmail("b.sam@email.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Create Customer - Duplicate National ID")
    void createCustomer_DuplicateNationalId_ThrowsException() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByNationalId(anyString())).thenReturn(true);

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> customerService.createCustomer(validCreateRequest));

        assertEquals("Customer with national ID ID123456789 already exists", exception.getMessage());
        verify(customerRepository).existsByEmail("b.sam@email.com");
        verify(customerRepository).existsByNationalId("ID123456789");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Get Customer By ID - Success")
    void getCustomerById_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(validCustomer));
        when(dtoMapper.toCustomerDto(validCustomer)).thenReturn(validCustomerDto);

        // When
        CustomerDto result = customerService.getCustomerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("blesson", result.getFirstName());
        verify(customerRepository).findById(1L);
        verify(dtoMapper).toCustomerDto(validCustomer);
    }

    @Test
    @DisplayName("Get Customer By Email - Success")
    void getCustomerByEmail_Success() {
        // Given
        when(customerRepository.findByEmail("b.sam@email.com")).thenReturn(Optional.of(validCustomer));
        when(dtoMapper.toCustomerDto(validCustomer)).thenReturn(validCustomerDto);

        // When
        CustomerDto result = customerService.getCustomerByEmail("b.sam@email.com");

        // Then
        assertNotNull(result);
        assertEquals("b.sam@email.com", result.getEmail());
        verify(customerRepository).findByEmail("b.sam@email.com");
    }


    @Test
    @DisplayName("Get All Customers - Success")
    void getAllCustomers_Success() {
        // Given
        when(customerRepository.findAll()).thenReturn(customerList);
        when(dtoMapper.toCustomerDtos(customerList)).thenReturn(customerDtoList);

        // When
        List<CustomerDto> result = customerService.getAllCustomers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(customerRepository).findAll();
        verify(dtoMapper).toCustomerDtos(customerList);
    }

    @Test
    @DisplayName("Search Customers - Success")
    void searchCustomers_Success() {
        // Given
        String searchTerm = "john";
        when(customerRepository.searchCustomers(searchTerm)).thenReturn(Arrays.asList(validCustomer));
        when(dtoMapper.toCustomerDtos(any())).thenReturn(Arrays.asList(validCustomerDto));

        // When
        List<CustomerDto> result = customerService.searchCustomers(searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("blesson", result.get(0).getFirstName());
        verify(customerRepository).searchCustomers(searchTerm);
    }

    @Test
    @DisplayName("Update Customer - Success")
    void updateCustomer_Success() {
        // Given
        CreateCustomerRequest updateRequest = CreateCustomerRequest.builder()
                .firstName("Johnny")
                .lastName("Doe")
                .email("johnny.doe@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, Country")
                .nationalId("ID123456789")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(1L)
                .firstName("Johnny")
                .lastName("Doe")
                .email("johnny.doe@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, Country")
                .nationalId("ID123456789")
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(validCustomer));
        when(customerRepository.existsByEmail("johnny.doe@email.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);
        when(dtoMapper.toCustomerDto(any(Customer.class))).thenReturn(validCustomerDto);

        // When
        CustomerDto result = customerService.updateCustomer(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }


    @Test
    @DisplayName("Update Customer Status - Success")
    void updateCustomerStatus_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(validCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(validCustomer);
        when(dtoMapper.toCustomerDto(any(Customer.class))).thenReturn(validCustomerDto);

        // When
        CustomerDto result = customerService.updateCustomerStatus(1L, Customer.CustomerStatus.SUSPENDED);

        // Then
        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
        verify(dtoMapper).toCustomerDto(any(Customer.class));
    }

    @Test
    @DisplayName("Delete Customer - Success")
    void deleteCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(validCustomer));
        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));
        verify(customerRepository).findById(1L);
        verify(customerRepository).delete(validCustomer);
    }

}
