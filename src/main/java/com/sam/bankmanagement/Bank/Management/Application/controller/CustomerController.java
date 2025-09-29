package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.CreateCustomerRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.service.CustomerService;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import com.sam.bankmanagement.Bank.Management.Application.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/customers")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;
    private final DtoMapper dtoMapper;

    public CustomerController(CustomerService customerService, DtoMapper dtoMapper) {
        this.customerService = customerService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieves customer details by customer ID (admin only or own profile)")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long id) {

        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();

        if (!currentCustomerId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CustomerDto customerDto = customerService.getCustomerById(id);
        return ResponseEntity.ok(customerDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Updates customer information (own profile only)")
    @PreAuthorize("hasRole('CUSTOMER')")
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

        Long currentCustomerId = SecurityUtil.getCurrentCustomerId();

        if (!currentCustomerId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CustomerDto customerDto = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(customerDto);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current customer profile",
            description = "Retrieve the profile of the currently authenticated customer")
    @SecurityRequirement(name = "Bearer Authentication")

    public ResponseEntity<CustomerDto> getCurrentCustomerProfile() {
        Customer currentCustomer = SecurityUtil.getCurrentCustomer();
        if (currentCustomer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CustomerDto customerDto = dtoMapper.toCustomerDto(currentCustomer);
        return ResponseEntity.ok(customerDto);
    }

}
