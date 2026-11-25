package org.delcom.ifs25045springbootcashflow.domain.repository;

import org.delcom.ifs25045springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25045springbootcashflow.domain.exception.DomainException;

import java.util.List;

/**
 * Port layanan AI untuk menghasilkan transaksi dari catatan keuangan bebas.
 */
public interface IFinanceAiService {

    /**
     * @param prompt catatan/instruksi keuangan dari pengguna
     * @return daftar transaksi hasil generasi
     * @throws DomainException jika komunikasi AI atau parsing gagal
     */
    List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException;
}
