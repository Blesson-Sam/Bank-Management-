package com.sam.bankmanagement.Bank.Management.Application.mapper;

import com.sam.bankmanagement.Bank.Management.Application.dto.AccountDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.CustomerDto;
import com.sam.bankmanagement.Bank.Management.Application.dto.TransactionDto;
import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DtoMapper {

    // Customer mappings
    @Mapping(target = "accounts", source = "accounts", qualifiedByName = "accountsToAccountDtos")
    CustomerDto toCustomerDto(Customer customer);

    List<CustomerDto> toCustomerDtos(List<Customer> customers);

    @Named("customerToCustomerDtoWithoutAccounts")
    @Mapping(target = "accounts", ignore = true)
    CustomerDto toCustomerDtoWithoutAccounts(Customer customer);

    // Account mappings
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer", qualifiedByName = "getCustomerFullName")
    AccountDto toAccountDto(Account account);

    @Named("accountsToAccountDtos")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer", qualifiedByName = "getCustomerFullName")
    List<AccountDto> toAccountDtos(List<Account> accounts);

    @Named("accountToAccountDtoWithoutCustomer")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", ignore = true)
    AccountDto toAccountDtoWithoutCustomer(Account account);

    // Transaction mappings
    @Mapping(target = "fromAccountNumber", source = "fromAccount.accountNumber")
    @Mapping(target = "toAccountNumber", source = "toAccount.accountNumber")
    TransactionDto toTransactionDto(Transaction transaction);

    List<TransactionDto> toTransactionDtos(List<Transaction> transactions);

    // Helper methods
    @Named("getCustomerFullName")
    default String getCustomerFullName(Customer customer) {
        if (customer == null) {
            return null;
        }
        return customer.getFirstName() + " " + customer.getLastName();
    }
}
