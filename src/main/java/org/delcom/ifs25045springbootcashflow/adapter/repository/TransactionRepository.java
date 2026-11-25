package org.delcom.ifs25045springbootcashflow.adapter.repository;

import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Repository transaksi in-memory berbasis generic {@link InMemoryRepository}.
 * Dipakai pada unit test; runtime memakai {@link JdbcTransactionRepository}.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer> implements ITransactionRepository {
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type) {
        return save(new Transaction(++idCounter, userId, description, amount, type));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(tx -> tx.getUserId() == userId);
    }
}
