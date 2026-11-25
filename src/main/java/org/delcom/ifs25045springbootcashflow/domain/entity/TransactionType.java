package org.delcom.ifs25045springbootcashflow.domain.entity;

import java.util.Locale;
import java.util.Optional;

/**
 * Jenis transaksi: pemasukan menambah saldo, pengeluaran mengurangi saldo.
 */
public enum TransactionType {
    INCOME,
    EXPENSE;

    public String label() {
        return switch (this) {
            case INCOME -> "Pemasukan";
            case EXPENSE -> "Pengeluaran";
        };
    }

    /**
     * Mengenali teks bebas (mis. keluaran AI): nama enum atau label, tanpa membedakan huruf.
     */
    public static Optional<TransactionType> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String normalized = text.trim().toUpperCase(Locale.ROOT);
        for (TransactionType type : values()) {
            if (type.name().equals(normalized) || type.label().toUpperCase(Locale.ROOT).equals(normalized)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
