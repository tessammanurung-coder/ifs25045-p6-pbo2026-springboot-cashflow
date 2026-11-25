package org.delcom.ifs25045springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionTest {

    @Test
    void shortConstructorSetsDefaults() {
        Transaction tx = new Transaction(1, 2, "Gaji", 5_000_000, TransactionType.INCOME);
        assertEquals(1, tx.getId());
        assertEquals(2, tx.getUserId());
        assertEquals("Gaji", tx.getDescription());
        assertEquals(5_000_000, tx.getAmount());
        assertEquals(TransactionType.INCOME, tx.getType());
        assertNull(tx.getProofPath());
        assertNotNull(tx.getCreatedAt());
    }

    @Test
    void fullConstructorKeepsValues() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction tx = new Transaction(1, 2, "Kopi", 20_000, TransactionType.EXPENSE, "proofs/a.png", now);
        assertEquals("proofs/a.png", tx.getProofPath());
        assertEquals(now, tx.getCreatedAt());
    }

    @Test
    void signedAmountDependsOnType() {
        assertEquals(100, new Transaction(1, 1, "a", 100, TransactionType.INCOME).signedAmount());
        assertEquals(-100, new Transaction(2, 1, "b", 100, TransactionType.EXPENSE).signedAmount());
    }

    @Test
    void mutatorsChangeState() {
        Transaction tx = new Transaction(1, 1, "a", 100, TransactionType.INCOME);
        tx.changeDescription("b");
        tx.changeAmount(250);
        tx.changeType(TransactionType.EXPENSE);
        tx.changeProofPath("proofs/x.png");
        assertEquals("b", tx.getDescription());
        assertEquals(250, tx.getAmount());
        assertEquals(TransactionType.EXPENSE, tx.getType());
        assertEquals("proofs/x.png", tx.getProofPath());
    }
}
