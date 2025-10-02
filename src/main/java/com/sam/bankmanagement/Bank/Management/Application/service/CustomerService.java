package com.sam.bankmanagement.Bank.Management.Application.service;

import com.sam.bankmanagement.Bank.Management.Application.dto.CreateCustomerRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.exception.ActiveAccountsExistException;
import com.sam.bankmanagement.Bank.Management.Application.exception.DuplicateResourceException;
import com.sam.bankmanagement.Bank.Management.Application.exception.ResourceNotFoundException;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import com.sam.bankmanagement.Bank.Management.Application.repository.CustomerRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final DtoMapper dtoMapper;

    @Transactional
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer with email " + request.getEmail() + " already exists");
        }

        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateResourceException("Customer with national ID " + request.getNationalId() + " already exists");
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .nationalId(request.getNationalId())
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", savedCustomer.getId());

        return dtoMapper.toCustomerDto(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return dtoMapper.toCustomerDto(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        log.info("Fetching customer with email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return dtoMapper.toCustomerDto(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        log.info("Fetching all customers");
        List<Customer> customers = customerRepository.findAll();
        return dtoMapper.toCustomerDtos(customers);
    }



    @Transactional
    public CustomerDto updateCustomer(Long id, CreateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (!customer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer with email " + request.getEmail() + " already exists");
        }

        if (!customer.getNationalId().equals(request.getNationalId()) &&
                customerRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateResourceException("Customer with national ID " + request.getNationalId() + " already exists");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setNationalId(request.getNationalId());

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());

        return dtoMapper.toCustomerDto(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (customer.getAccounts() != null && !customer.getAccounts().isEmpty()) {
            boolean hasActiveAccounts = customer.getAccounts().stream()
                    .anyMatch(account -> account.getStatus() == Account.AccountStatus.ACTIVE);
            if (hasActiveAccounts) {
                throw new ActiveAccountsExistException(id);
            }
        }

        customerRepository.delete(customer);
        log.info("Customer deleted successfully with ID: {}", id);
    }

    @Transactional
    public CustomerDto updateCustomerStatus(Long id, Customer.CustomerStatus status) {
        log.info("Updating customer status for ID: {} to {}", id, status);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customer.setStatus(status);
        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer status updated successfully for ID: {}", id);
        return dtoMapper.toCustomerDto(updatedCustomer);
    }
}
