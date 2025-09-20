package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.CreateCustomerRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/customers")
@Tag(name = "Customer Management", description = "APIs for managing bank customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Creates a new customer in the bank system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Customer already exists")
    })
    public ResponseEntity<CustomerDto> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerDto customerDto = customerService.createCustomer(request);
        return new ResponseEntity<>(customerDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieves customer details by customer ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long id) {
        CustomerDto customerDto = customerService.getCustomerById(id);
        return ResponseEntity.ok(customerDto);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email", description = "Retrieves customer details by email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CustomerDto> getCustomerByEmail(
            @Parameter(description = "Customer email", required = true)
            @PathVariable String email) {
        CustomerDto customerDto = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customerDto);
    }

    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieves all customers in the system")
    @ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        List<CustomerDto> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search customers by name or email")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<List<CustomerDto>> searchCustomers(
            @Parameter(description = "Search term for customer name or email", required = true)
            @RequestParam String searchTerm) {
        List<CustomerDto> customers = customerService.searchCustomers(searchTerm);
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Updates customer information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Email or National ID already exists")
    })
    public ResponseEntity<CustomerDto> updateCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerDto customerDto = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(customerDto);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update customer status", description = "Updates customer status (ACTIVE, INACTIVE, SUSPENDED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CustomerDto> updateCustomerStatus(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "New status", required = true)
            @RequestParam Customer.CustomerStatus status) {
        CustomerDto customerDto = customerService.updateCustomerStatus(id, status);
        return ResponseEntity.ok(customerDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Deletes a customer (only if no active accounts)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Customer has active accounts")
    })
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
