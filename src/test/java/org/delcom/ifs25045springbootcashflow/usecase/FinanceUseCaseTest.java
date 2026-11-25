package org.delcom.ifs25045springbootcashflow.usecase;

import org.delcom.ifs25045springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25045springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25045springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25045springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int USER = 1;
    private static final int OTHER = 2;

    private TransactionRepository repository;
    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
        useCase = new FinanceUseCase(repository);
    }

    @Test
    void addTransactionTrimsAndSaves() throws Exception {
        Transaction tx = useCase.addTransaction(USER, "  Gaji  ", " 5000000 ", TransactionType.INCOME);
        assertEquals("Gaji", tx.getDescription());
        assertEquals(5_000_000, tx.getAmount());
        assertEquals(1, repository.findByUserId(USER).size());
    }

    @Test
    void addTransactionValidatesInput() {
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, null, "100", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "  ", "100", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "a", null, TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "a", "  ", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "a", "abc", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "a", "1.5", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "a", "99999999999999999999", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "a", "0", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "a", "-10", TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "a", "10", null));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void parseAmountMessages() {
        ValidationException blank = assertThrows(ValidationException.class, () -> FinanceUseCase.parseAmount(""));
        assertEquals("Jumlah tidak boleh kosong", blank.getMessage());
        ValidationException nan = assertThrows(ValidationException.class, () -> FinanceUseCase.parseAmount("x"));
        assertTrue(nan.getMessage().contains("angka"));
        ValidationException zero = assertThrows(ValidationException.class, () -> FinanceUseCase.parseAmount("0"));
        assertEquals("Jumlah harus lebih dari 0", zero.getMessage());
    }

    @Test
    void listDefaultsToNewestFirstAndOnlyOwnData() throws Exception {
        useCase.addTransaction(USER, "Pertama", "100", TransactionType.INCOME);
        useCase.addTransaction(USER, "Kedua", "200", TransactionType.EXPENSE);
        useCase.addTransaction(OTHER, "Milik lain", "300", TransactionType.INCOME);

        List<Transaction> list = useCase.getTransactionsByUser(USER);
        assertEquals(2, list.size());
        assertEquals("Kedua", list.get(0).getDescription());
    }

    @Test
    void listSupportsSearchFilterAndSort() throws Exception {
        useCase.addTransaction(USER, "Gaji Bulanan", "5000", TransactionType.INCOME);
        useCase.addTransaction(USER, "Makan siang", "25", TransactionType.EXPENSE);
        useCase.addTransaction(USER, "Makan malam", "75", TransactionType.EXPENSE);

        assertEquals(2, useCase.listTransactions(USER, "makan", null, null).size());
        assertEquals(3, useCase.listTransactions(USER, "  ", TransactionTypeFilter.ALL, SortOption.NEWEST_FIRST).size());
        assertEquals(3, useCase.listTransactions(USER, null, TransactionTypeFilter.ALL, SortOption.NEWEST_FIRST).size());
        assertEquals(1, useCase.listTransactions(USER, null, TransactionTypeFilter.INCOME, null).size());
        assertEquals(0, useCase.listTransactions(USER, "gaji", TransactionTypeFilter.EXPENSE, null).size());

        List<Transaction> byAmount = useCase.listTransactions(USER, null, TransactionTypeFilter.ALL, SortOption.AMOUNT_DESC);
        assertEquals(5000, byAmount.get(0).getAmount());
    }

    @Test
    void balanceAddsIncomeAndSubtractsExpense() throws Exception {
        useCase.addTransaction(USER, "Gaji", "6000000", TransactionType.INCOME);
        useCase.addTransaction(USER, "Sewa", "1000000", TransactionType.EXPENSE);
        useCase.addTransaction(USER, "Makan", "25000", TransactionType.EXPENSE);
        useCase.addTransaction(OTHER, "Bukan saya", "999", TransactionType.INCOME);

        FinanceUseCase.Balance balance = useCase.getBalance(USER);
        assertEquals(3, balance.count());
        assertEquals(6_000_000, balance.totalIncome());
        assertEquals(1_025_000, balance.totalExpense());
        assertEquals(4_975_000, balance.balance());

        FinanceUseCase.Balance empty = useCase.getBalance(99);
        assertEquals(0, empty.count());
        assertEquals(0, empty.balance());
    }

    @Test
    void balanceStaysConsistentAfterRemove() throws Exception {
        Transaction income = useCase.addTransaction(USER, "Gaji", "1000", TransactionType.INCOME);
        useCase.addTransaction(USER, "Jajan", "300", TransactionType.EXPENSE);
        assertEquals(700, useCase.getBalance(USER).balance());

        useCase.removeTransactionForUser(USER, income.getId());
        assertEquals(-300, useCase.getBalance(USER).balance());
    }

    @Test
    void removeChecksExistenceAndOwnership() throws Exception {
        Transaction tx = useCase.addTransaction(USER, "a", "10", TransactionType.INCOME);
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransactionForUser(USER, 999));
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(OTHER, tx.getId()));
        assertEquals(1, repository.findAll().size());

        useCase.removeTransactionForUser(USER, tx.getId());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void updateChangesOnlyProvidedFields() throws Exception {
        Transaction tx = useCase.addTransaction(USER, "Lama", "100", TransactionType.INCOME);

        useCase.updateTransactionForUser(USER, tx.getId(), " Baru ", "250", TransactionType.EXPENSE, "proofs/a.png", false);
        assertEquals("Baru", tx.getDescription());
        assertEquals(250, tx.getAmount());
        assertEquals(TransactionType.EXPENSE, tx.getType());
        assertEquals("proofs/a.png", tx.getProofPath());

        useCase.updateTransactionForUser(USER, tx.getId(), null, null, null, null, false);
        assertEquals("Baru", tx.getDescription());
        assertEquals("proofs/a.png", tx.getProofPath());

        useCase.updateTransactionForUser(USER, tx.getId(), null, null, null, "proofs/ignored.png", true);
        assertNull(tx.getProofPath());
    }

    @Test
    void updateIsAllOrNothingAndChecksOwnership() throws Exception {
        Transaction tx = useCase.addTransaction(USER, "Lama", "100", TransactionType.INCOME);

        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(USER, tx.getId(), "Baru", "abc", null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(USER, tx.getId(), " ", null, null, null, false));
        assertEquals("Lama", tx.getDescription());
        assertEquals(100, tx.getAmount());

        assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransactionForUser(USER, 999, null, null, null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(OTHER, tx.getId(), "x", null, null, null, false));
    }
}
