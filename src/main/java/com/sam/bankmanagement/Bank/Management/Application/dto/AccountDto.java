package com.sam.bankmanagement.Bank.Management.Application.dto;

import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String accountNumber;
    private Account.AccountType accountType;
    private BigDecimal balance;
    private BigDecimal interestRate;
    private BigDecimal accruedInterest;
    private LocalDateTime lastInterestCalculated;
    private Account.AccountStatus status;
    private Long customerId;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

