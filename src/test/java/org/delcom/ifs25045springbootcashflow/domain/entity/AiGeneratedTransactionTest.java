package org.delcom.ifs25045springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiGeneratedTransactionTest {

    @Test
    void trimsDescription() {
        AiGeneratedTransaction item = new AiGeneratedTransaction("  Gaji  ", 100, TransactionType.INCOME);
        assertEquals("Gaji", item.description());
        assertEquals(100, item.amount());
        assertEquals(TransactionType.INCOME, item.type());
    }

    @Test
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new AiGeneratedTransaction(null, 1, TransactionType.INCOME));
        assertThrows(IllegalArgumentException.class, () -> new AiGeneratedTransaction("  ", 1, TransactionType.INCOME));
        assertThrows(IllegalArgumentException.class, () -> new AiGeneratedTransaction("a", 0, TransactionType.INCOME));
        assertThrows(IllegalArgumentException.class, () -> new AiGeneratedTransaction("a", -5, TransactionType.INCOME));
        assertThrows(IllegalArgumentException.class, () -> new AiGeneratedTransaction("a", 5, null));
    }
}
