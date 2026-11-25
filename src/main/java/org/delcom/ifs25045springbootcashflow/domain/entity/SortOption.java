package org.delcom.ifs25045springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi. Setiap opsi membawa comparator-nya sendiri
 * sehingga logika pengurutan terpusat di domain.
 */
public enum SortOption {
    /** Transaksi terbaru di atas (default). */
    NEWEST_FIRST(Comparator
            .comparing(Transaction::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Transaction::getId)
            .reversed()),

    /** Transaksi terlama di atas. */
    OLDEST_FIRST(Comparator
            .comparing(Transaction::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Transaction::getId)),

    /** Jumlah terbesar di atas. */
    AMOUNT_DESC(Comparator.comparingLong(Transaction::getAmount).reversed()
            .thenComparing(Transaction::getId, Comparator.reverseOrder())),

    /** Jumlah terkecil di atas. */
    AMOUNT_ASC(Comparator.comparingLong(Transaction::getAmount)
            .thenComparing(Transaction::getId, Comparator.reverseOrder())),

    /** Keterangan A-Z (tanpa membedakan huruf). */
    DESCRIPTION_ASC(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER)),

    /** Keterangan Z-A (tanpa membedakan huruf). */
    DESCRIPTION_DESC(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER).reversed());

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }

    /** Label singkat untuk ditampilkan di UI. */
    public String label() {
        return switch (this) {
            case NEWEST_FIRST -> "Terbaru";
            case OLDEST_FIRST -> "Terlama";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case AMOUNT_ASC -> "Jumlah terkecil";
            case DESCRIPTION_ASC -> "Keterangan A-Z";
            case DESCRIPTION_DESC -> "Keterangan Z-A";
        };
    }
}
