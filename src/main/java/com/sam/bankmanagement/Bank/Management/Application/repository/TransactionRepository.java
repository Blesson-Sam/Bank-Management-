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

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByFromAccountIdOrderByCreatedAtDesc(Long accountId);

    List<Transaction> findByToAccountIdOrderByCreatedAtDesc(Long accountId);

    List<Transaction> findByType(Transaction.TransactionType type);

    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccount.accountNumber = :accountNumber OR t.toAccount.accountNumber = :accountNumber) " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountIdAndDateRange(@Param("accountId") Long accountId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE " +
            "t.toAccount.id = :accountId AND t.type = 'DEPOSIT' AND t.status = 'COMPLETED' " +
            "AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalDeposits(@Param("accountId") Long accountId,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE " +
            "t.fromAccount.id = :accountId AND t.type = 'WITHDRAWAL' AND t.status = 'COMPLETED' " +
            "AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalWithdrawals(@Param("accountId") Long accountId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
            "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
            "AND t.status = 'COMPLETED' " +
            "AND DATE(t.createdAt) = CURRENT_DATE")
    long countTodaysTransactions(@Param("accountId") Long accountId);
}
