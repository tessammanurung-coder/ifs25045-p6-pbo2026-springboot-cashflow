package org.delcom.ifs25045springbootcashflow.usecase;

import org.delcom.ifs25045springbootcashflow.adapter.ai.InMemoryFinanceAiService;
import org.delcom.ifs25045springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25045springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25045springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinanceAiUseCaseTest {

    private InMemoryFinanceAiService aiService;
    private TransactionRepository repository;
    private FinanceAiUseCase useCase;

    @BeforeEach
    void setUp() {
        aiService = new InMemoryFinanceAiService();
        repository = new TransactionRepository();
        useCase = new FinanceAiUseCase(aiService, repository);
    }

    @Test
    void generateReturnsPreviewWithoutSaving() throws Exception {
        List<AiGeneratedTransaction> result = useCase.generateTransactions("  gajian dan makan  ");
        assertEquals(2, result.size());
        assertEquals(TransactionType.INCOME, result.get(0).type());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void generateRejectsBlankPromptAndPropagatesAiFailure() {
        assertThrows(ValidationException.class, () -> useCase.generateTransactions(null));
        assertThrows(ValidationException.class, () -> useCase.generateTransactions("   "));

        aiService.setSimulateFailure(true);
        assertThrows(DomainException.class, () -> useCase.generateTransactions("apa saja"));
    }

    @Test
    void saveGeneratedPersistsAllItemsForUser() throws Exception {
        List<AiGeneratedTransaction> items = List.of(
                new AiGeneratedTransaction("Gaji", 1000, TransactionType.INCOME),
                new AiGeneratedTransaction("Kopi", 20, TransactionType.EXPENSE)
        );
        List<Transaction> saved = useCase.saveGeneratedTransactions(1, items);
        assertEquals(2, saved.size());
        assertEquals(2, repository.findByUserId(1).size());
        assertEquals(0, repository.findByUserId(2).size());
    }

    @Test
    void saveGeneratedRejectsEmptyOrNull() {
        assertThrows(ValidationException.class, () -> useCase.saveGeneratedTransactions(1, null));
        assertThrows(ValidationException.class, () -> useCase.saveGeneratedTransactions(1, Collections.emptyList()));
    }
}
