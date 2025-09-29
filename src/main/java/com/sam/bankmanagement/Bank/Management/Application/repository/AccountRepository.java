package com.sam.bankmanagement.Bank.Management.Application.repository;

import com.sam.bankmanagement.Bank.Management.Application.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    Page<Account> findByCustomerId(Long customerId, Pageable pageable);

    List<Account> findByAccountType(Account.AccountType accountType);

    List<Account> findByStatus(Account.AccountStatus status);

    long countByStatus(Account.AccountStatus status);

    List<Account> findByAccountTypeAndStatus(Account.AccountType accountType, Account.AccountStatus status);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.customer.id = :customerId AND a.status = :status")
    List<Account> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                            @Param("status") Account.AccountStatus status);

    @Query("SELECT a FROM Account a WHERE a.accountType = :accountType AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByType(@Param("accountType") Account.AccountType accountType);

    @Query("SELECT a FROM Account a WHERE a.status = 'ACTIVE' AND " +
            "(a.lastInterestCalculated IS NULL OR a.lastInterestCalculated < :cutoffDate)")
    List<Account> findAccountsForInterestCalculation(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);
}
