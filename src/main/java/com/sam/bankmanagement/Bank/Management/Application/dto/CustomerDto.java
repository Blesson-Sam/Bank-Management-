package com.sam.bankmanagement.Bank.Management.Application.dto;

import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String nationalId;
    private String gender;
    private Customer.CustomerStatus status;
    private List<AccountDto> accounts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
