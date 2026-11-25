package org.delcom.ifs25045springbootcashflow.adapter.presenter;

import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income = new Transaction(
            1, 1, "Gaji", 1_000_000, TransactionType.INCOME, null, LocalDateTime.of(2026, 3, 4, 9, 5));
    private final Transaction expense = new Transaction(
            2, 1, "Makan", 25_000, TransactionType.EXPENSE, null, LocalDateTime.of(2026, 3, 4, 12, 30));

    @Test
    void formatsAmountWithThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatAmount(1_000_000));
        assertEquals("Rp 0.00", presenter.formatAmount(0));
        assertEquals("Rp 999.00", presenter.formatAmount(999));
        assertEquals("Rp -2,500.00", presenter.formatAmount(-2500));
        assertEquals("Rp 9,223,372,036,854,775,807.00", presenter.formatAmount(Long.MAX_VALUE));
    }

    @Test
    void formatsTransactionLineAndBalance() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000));
    }

    @Test
    void formatsCreatedAt() {
        assertEquals("04/03/2026 09:05", presenter.formatCreatedAt(income));
    }

    @Test
    void formatsListAndEmptyList() {
        String text = presenter.formatList(List.of(income, expense), "Daftar:", "kosong");
        assertTrue(text.startsWith("Daftar:\n[1] Gaji"));
        assertTrue(text.contains("\n[2] Makan"));
        assertEquals("Daftar:\nkosong", presenter.formatList(List.of(), "Daftar:", "kosong"));
    }

    @Test
    void messages() {
        assertEquals("Berhasil menambah transaksi: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 7.", presenter.showRemoveFailed(7));
        assertEquals("Berhasil mengubah transaksi.", presenter.showUpdateSuccess());
        assertEquals("[!] Gagal mengubah transaksi dengan ID: 7.", presenter.showUpdateFailed(7));
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] salah", presenter.showValidationError("salah"));
    }
}
