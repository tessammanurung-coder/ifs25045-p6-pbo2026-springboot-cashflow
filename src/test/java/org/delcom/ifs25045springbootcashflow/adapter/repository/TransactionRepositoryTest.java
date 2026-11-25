package org.delcom.ifs25045springbootcashflow.adapter.repository;

import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    @Test
    void genericCrudAndFinders() {
        TransactionRepository repository = new TransactionRepository();
        Transaction a = repository.save(1, "Gaji", 1000, TransactionType.INCOME);
        Transaction b = repository.save(2, "Makan", 50, TransactionType.EXPENSE);
        Transaction c = repository.save(new Transaction(99, 1, "Manual", 5, TransactionType.EXPENSE));

        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(99, c.getId());
        assertEquals(3, repository.findAll().size());
        assertEquals(2, repository.findByUserId(1).size());
        assertEquals(1, repository.findBy(tx -> tx.getAmount() == 50).size());
        assertTrue(repository.findById(1).isPresent());
        assertTrue(repository.findById(1234).isEmpty());

        a.changeDescription("Gaji baru");
        repository.update(a);
        assertEquals("Gaji baru", repository.findById(1).orElseThrow().getDescription());

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
        assertEquals(2, repository.findAll().size());
    }
}
