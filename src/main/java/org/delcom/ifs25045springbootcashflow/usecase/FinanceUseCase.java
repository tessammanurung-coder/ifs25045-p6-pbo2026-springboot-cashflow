package org.delcom.ifs25045springbootcashflow.usecase;

import org.delcom.ifs25045springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25045springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25045springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25045springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Use case logika bisnis catatan keuangan.
 * Tidak melakukan I/O UI: hanya memproses data dan melempar exception domain.
 */
public class FinanceUseCase {

    /** Ringkasan keuangan satu pengguna. */
    public record Balance(int count, long totalIncome, long totalExpense) {
        /** Saldo = total pemasukan - total pengeluaran. */
        public long balance() {
            return totalIncome - totalExpense;
        }
    }

    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Semua transaksi milik satu pengguna, terbaru di atas. */
    public List<Transaction> getTransactionsByUser(int userId) {
        return listTransactions(userId, null, TransactionTypeFilter.ALL, SortOption.NEWEST_FIRST);
    }

    /**
     * Transaksi pengguna dengan pencarian keterangan, filter tipe, dan urutan.
     * Keyword kosong diabaikan; filter/sort null memakai default (semua + terbaru).
     */
    public List<Transaction> listTransactions(
            int userId,
            String keyword,
            TransactionTypeFilter typeFilter,
            SortOption sortOption
    ) {
        TransactionTypeFilter filter = typeFilter == null ? TransactionTypeFilter.ALL : typeFilter;
        SortOption sort = sortOption == null ? SortOption.NEWEST_FIRST : sortOption;

        Stream<Transaction> stream = transactionRepository.findByUserId(userId).stream()
                .filter(filter::matches);

        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.trim().toLowerCase();
            stream = stream.filter(tx -> tx.getDescription().toLowerCase().contains(lowerKeyword));
        }
        return stream.sorted(sort.comparator()).toList();
    }

    /** Menghitung total transaksi, pemasukan, pengeluaran, dan saldo pengguna. */
    public Balance getBalance(int userId) {
        List<Transaction> all = transactionRepository.findByUserId(userId);
        long income = all.stream()
                .filter(tx -> tx.getType() == TransactionType.INCOME)
                .mapToLong(Transaction::getAmount)
                .sum();
        long expense = all.stream()
                .filter(tx -> tx.getType() == TransactionType.EXPENSE)
                .mapToLong(Transaction::getAmount)
                .sum();
        return new Balance(all.size(), income, expense);
    }

    /** Menambahkan transaksi baru setelah validasi keterangan, jumlah, dan tipe. */
    public Transaction addTransaction(int userId, String description, String amountText, TransactionType type)
            throws ValidationException {
        String safeDescription = requireDescription(description);
        long amount = parseAmount(amountText);
        TransactionType safeType = requireType(type);
        return transactionRepository.save(userId, safeDescription, amount, safeType);
    }

    /** Menghapus transaksi milik pengguna; gagal jika tidak ada atau bukan miliknya. */
    public void removeTransactionForUser(int userId, int id) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwned(userId, id);
        transactionRepository.deleteById(transaction.getId());
    }

    /**
     * Mengubah transaksi milik pengguna. Parameter {@code null} berarti tidak diubah
     * (kecuali {@code clearProof=true} untuk menghapus bukti). Semua validasi dilakukan
     * sebelum ada perubahan, sehingga update bersifat all-or-nothing.
     */
    public void updateTransactionForUser(
            int userId,
            int id,
            String description,
            String amountText,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwned(userId, id);

        String newDescription = description == null ? null : requireDescription(description);
        Long newAmount = amountText == null ? null : parseAmount(amountText);

        if (newDescription != null) {
            transaction.changeDescription(newDescription);
        }
        if (newAmount != null) {
            transaction.changeAmount(newAmount);
        }
        if (type != null) {
            transaction.changeType(type);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (proofPath != null) {
            transaction.changeProofPath(proofPath);
        }
        transactionRepository.update(transaction);
    }

    private Transaction requireOwned(int userId, int id) throws EntityNotFoundException, ValidationException {
        Optional<Transaction> found = transactionRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("Transaksi", id);
        }
        Transaction transaction = found.get();
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi tidak milik pengguna ini");
        }
        return transaction;
    }

    private static String requireDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        return description.trim();
    }

    private static TransactionType requireType(TransactionType type) throws ValidationException {
        if (type == null) {
            throw new ValidationException("Tipe transaksi wajib dipilih");
        }
        return type;
    }

    /** Mengubah teks jumlah menjadi bilangan bulat positif. */
    static long parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        long value;
        try {
            value = Long.parseLong(amountText.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka bulat (tanpa titik/koma)");
        }
        if (value <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        return value;
    }
}
