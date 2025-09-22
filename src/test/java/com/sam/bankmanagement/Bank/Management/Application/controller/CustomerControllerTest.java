package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sam.bankmanagement.Bank.Management.Application.dto.CreateCustomerRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.exception.DuplicateResourceException;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@DisplayName("Customer Controller Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateCustomerRequest validCreateRequest;
    private CustomerDto validCustomerDto;
    private List<CustomerDto> customerDtoList;

    @BeforeEach
    void setUp() {
        validCreateRequest = CreateCustomerRequest.builder()
                .firstName("Blesson")
                .lastName("Sam")
                .email("b.sam@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, Country")
                .nationalId("ID123456789")
                .build();

        validCustomerDto = CustomerDto.builder()
                .id(1L)
                .firstName("Blesson")
                .lastName("Sam")
                .email("b.sam@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, Country")
                .nationalId("ID123456789")
                .status(Customer.CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CustomerDto customer2 = CustomerDto.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@email.com")
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        customerDtoList = Arrays.asList(validCustomerDto, customer2);
    }

    @Test
    @DisplayName("Create Customer - Success")
    void createCustomer_Success() throws Exception {

        when(customerService.createCustomer(any(CreateCustomerRequest.class))).thenReturn(validCustomerDto);

        mockMvc.perform(post("/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Blesson"))
                .andExpect(jsonPath("$.lastName").value("Sam"))
                .andExpect(jsonPath("$.email").value("b.sam@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        verify(customerService).createCustomer(any(CreateCustomerRequest.class));
    }

    @Test
    @DisplayName("Create Customer - Duplicate Email")
    void createCustomer_DuplicateEmail_ReturnsConflict() throws Exception {

        when(customerService.createCustomer(any(CreateCustomerRequest.class)))
                .thenThrow(new DuplicateResourceException("Customer with email john.doe@email.com already exists"));

        mockMvc.perform(post("/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Customer with email john.doe@email.com already exists"));

        verify(customerService).createCustomer(any(CreateCustomerRequest.class));
    }

    @Test
    @DisplayName("Get Customer By ID - Success")
    void getCustomerById_Success() throws Exception {

        when(customerService.getCustomerById(1L)).thenReturn(validCustomerDto);

        mockMvc.perform(get("/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Blesson"))
                .andExpect(jsonPath("$.lastName").value("Sam"))
                .andExpect(jsonPath("$.email").value("b.sam@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(customerService).getCustomerById(1L);
    }


    @Test
    @DisplayName("Get All Customers - Success")
    void getAllCustomers_Success() throws Exception {
        // Given
        when(customerService.getAllCustomers()).thenReturn(customerDtoList);

        // When & Then
        mockMvc.perform(get("/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(customerDtoList.size()))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(customerService).getAllCustomers();
    }

    @Test
    @DisplayName("Update Customer - Success")
    void updateCustomer_Success() throws Exception {

        CustomerDto updatedCustomer = validCustomerDto.toBuilder()
                .firstName("Johnny")
                .build();
        when(customerService.updateCustomer(eq(1L), any(CreateCustomerRequest.class)))
                .thenReturn(updatedCustomer);

        CreateCustomerRequest updateRequest = CreateCustomerRequest.builder()
                .firstName("Johnny")
                .lastName("Sam")
                .email("john.sam@email.com")
                .phone("+1234567890")
                .address("123 Main St, City, Country")
                .nationalId("ID123456789")
                .build();

        mockMvc.perform(put("/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Sam"));

        verify(customerService).updateCustomer(eq(1L), any(CreateCustomerRequest.class));
    }

    @Test
    @DisplayName("Delete Customer - Success")
    void deleteCustomer_Success() throws Exception {
        mockMvc.perform(delete("/v1/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(1L);
    }
}

