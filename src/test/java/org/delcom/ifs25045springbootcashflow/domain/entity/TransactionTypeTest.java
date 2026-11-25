package org.delcom.ifs25045springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTypeTest {

    @Test
    void labels() {
        assertEquals("Pemasukan", TransactionType.INCOME.label());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.label());
    }

    @Test
    void parseAcceptsNamesAndLabelsIgnoringCase() {
        assertEquals(TransactionType.INCOME, TransactionType.parse("income").orElseThrow());
        assertEquals(TransactionType.EXPENSE, TransactionType.parse(" EXPENSE ").orElseThrow());
        assertEquals(TransactionType.INCOME, TransactionType.parse("Pemasukan").orElseThrow());
        assertEquals(TransactionType.EXPENSE, TransactionType.parse("pengeluaran").orElseThrow());
    }

    @Test
    void parseRejectsUnknownAndNull() {
        assertTrue(TransactionType.parse("transfer").isEmpty());
        assertTrue(TransactionType.parse(null).isEmpty());
    }
}
