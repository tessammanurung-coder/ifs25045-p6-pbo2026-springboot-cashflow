package org.delcom.ifs25045springbootcashflow.domain.entity;

/**
 * Transaksi hasil generasi AI sebelum disimpan ke basis data.
 */
public record AiGeneratedTransaction(String description, long amount, TransactionType type) {

    public AiGeneratedTransaction {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Keterangan transaksi tidak boleh kosong");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah transaksi harus lebih dari 0");
        }
        if (type == null) {
            throw new IllegalArgumentException("Tipe transaksi tidak boleh kosong");
        }
        description = description.trim();
    }
}
