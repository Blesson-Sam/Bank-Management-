package com.sam.bankmanagement.Bank.Management.Application.config;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import com.sam.bankmanagement.Bank.Management.Application.mapper.DtoMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MapperConfig {
    @Bean
    public DtoMapper dtoMapper() {
        return new DtoMapper() {
            @Override
            public CustomerDto toCustomerDto(Customer customer) {
                return null;
            }

            @Override
            public List<CustomerDto> toCustomerDtos(List<Customer> customers) {
                return List.of();
            }

            @Override
            public CustomerDto toCustomerDtoWithoutAccounts(Customer customer) {
                return null;
            }

            @Override
            public AccountDto toAccountDto(Account account) {
                return null;
            }

            @Override
            public List<AccountDto> toAccountDtos(List<Account> accounts) {
                return List.of();
            }

            @Override
            public AccountDto toAccountDtoWithoutCustomer(Account account) {
                return null;
            }

            @Override
            public TransactionDto toTransactionDto(Transaction transaction) {
                return null;
            }

            @Override
            public List<TransactionDto> toTransactionDtos(List<Transaction> transactions) {
                return List.of();
            }
            // implement methods manually or throw UnsupportedOperationException
        };
    }
}
