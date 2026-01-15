package com.gabriel.picpayteste.repositories;

import com.gabriel.picpayteste.domain.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
