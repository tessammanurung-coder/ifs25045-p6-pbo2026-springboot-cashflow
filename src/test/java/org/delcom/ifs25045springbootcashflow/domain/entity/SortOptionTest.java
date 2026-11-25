package org.delcom.ifs25045springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private static final LocalDateTime OLD = LocalDateTime.of(2026, 1, 1, 8, 0);
    private static final LocalDateTime NEW = LocalDateTime.of(2026, 1, 2, 8, 0);

    private List<Transaction> sample() {
        List<Transaction> list = new ArrayList<>();
        list.add(new Transaction(1, 1, "beta", 300, TransactionType.INCOME, null, OLD));
        list.add(new Transaction(2, 1, "Alpha", 100, TransactionType.EXPENSE, null, NEW));
        list.add(new Transaction(3, 1, "gamma", 100, TransactionType.EXPENSE, null, null));
        return list;
    }

    @Test
    void newestFirstPutsLatestOnTop() {
        List<Transaction> list = sample();
        list.sort(SortOption.NEWEST_FIRST.comparator());
        assertEquals(List.of(3, 2, 1), list.stream().map(Transaction::getId).toList());
    }

    @Test
    void oldestFirstPutsEarliestOnTop() {
        List<Transaction> list = sample();
        list.sort(SortOption.OLDEST_FIRST.comparator());
        assertEquals(List.of(1, 2, 3), list.stream().map(Transaction::getId).toList());
    }

    @Test
    void sortsByAmount() {
        List<Transaction> list = sample();
        list.sort(SortOption.AMOUNT_DESC.comparator());
        assertEquals(List.of(1, 3, 2), list.stream().map(Transaction::getId).toList());
        list.sort(SortOption.AMOUNT_ASC.comparator());
        assertEquals(List.of(3, 2, 1), list.stream().map(Transaction::getId).toList());
    }

    @Test
    void sortsByDescriptionIgnoringCase() {
        List<Transaction> list = sample();
        list.sort(SortOption.DESCRIPTION_ASC.comparator());
        assertEquals("Alpha", list.get(0).getDescription());
        list.sort(SortOption.DESCRIPTION_DESC.comparator());
        assertEquals("gamma", list.get(0).getDescription());
    }

    @Test
    void labelsAreReadable() {
        assertEquals("Terbaru", SortOption.NEWEST_FIRST.label());
        assertEquals("Terlama", SortOption.OLDEST_FIRST.label());
        assertEquals("Jumlah terbesar", SortOption.AMOUNT_DESC.label());
        assertEquals("Jumlah terkecil", SortOption.AMOUNT_ASC.label());
        assertEquals("Keterangan A-Z", SortOption.DESCRIPTION_ASC.label());
        assertEquals("Keterangan Z-A", SortOption.DESCRIPTION_DESC.label());
    }
}
