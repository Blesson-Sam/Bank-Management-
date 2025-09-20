package com.sam.bankmanagement.Bank.Management.Application.dto;

import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
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
public class TransactionDto {
    private Long id;
    private String transactionId;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private String description;
    private String fromAccountNumber;
    private String toAccountNumber;
    private Transaction.TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}