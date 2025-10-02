package com.sam.bankmanagement.Bank.Management.Application.repository;

import com.sam.bankmanagement.Bank.Management.Application.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByStatus(Transaction.TransactionStatus status, Pageable pageable);

    Page<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);


    List<Transaction> findByFromAccountIdAndStatus(Long fromAccountId, Transaction.TransactionStatus status);

    List<Transaction> findByToAccountIdAndStatus(Long toAccountId, Transaction.TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccount.accountNumber = :accountNumber OR t.toAccount.accountNumber = :accountNumber) " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccount.accountNumber = :accountNumber OR t.toAccount.accountNumber = :accountNumber) " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccount.customer.id = :customerId OR t.toAccount.customer.id = :customerId) " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountIdAndDateRange(@Param("accountId") Long accountId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);


}
