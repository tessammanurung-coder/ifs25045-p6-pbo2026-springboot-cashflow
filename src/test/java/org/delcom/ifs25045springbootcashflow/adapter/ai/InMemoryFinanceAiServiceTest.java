package org.delcom.ifs25045springbootcashflow.adapter.ai;

import org.delcom.ifs25045springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25045springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryFinanceAiServiceTest {

    @Test
    void generatesDefaultTransactions() throws Exception {
        List<AiGeneratedTransaction> result = new InMemoryFinanceAiService().generateTransactions("gajian");
        assertEquals(2, result.size());
        assertEquals(TransactionType.INCOME, result.get(0).type());
        assertEquals(TransactionType.EXPENSE, result.get(1).type());
    }

    @Test
    void usesCannedResponsesWhenProvided() throws Exception {
        InMemoryFinanceAiService service = new InMemoryFinanceAiService(List.of(
                new AiGeneratedTransaction("Custom 1", 10, TransactionType.INCOME)));
        service.addCannedResponse("Custom 2", 20, TransactionType.EXPENSE);

        List<AiGeneratedTransaction> result = service.generateTransactions("apa saja");
        assertEquals(2, result.size());
        assertEquals("Custom 2", result.get(1).description());
    }

    @Test
    void validatesPromptAndSimulatesFailure() {
        InMemoryFinanceAiService service = new InMemoryFinanceAiService();
        assertThrows(ValidationException.class, () -> service.generateTransactions(null));
        assertThrows(ValidationException.class, () -> service.generateTransactions("  "));

        service.setSimulateFailure(true);
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
    }
}
