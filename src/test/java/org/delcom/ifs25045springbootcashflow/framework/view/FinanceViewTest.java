package org.delcom.ifs25045springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.delcom.ifs25045springbootcashflow.adapter.ai.InMemoryFinanceAiService;
import org.delcom.ifs25045springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25045springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25045springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25045springbootcashflow.domain.entity.Role;
import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.entity.User;
import org.delcom.ifs25045springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25045springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25045springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._doubleClickItem;
import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceViewTest {

    private static final int USER_ID = 1;

    @TempDir
    Path tempDir;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        TransactionRepository repository = new TransactionRepository();
        useCase = new FinanceUseCase(repository);
        FinanceAiUseCase aiUseCase = new FinanceAiUseCase(new InMemoryFinanceAiService(), repository);
        MockVaadin.setup();
        AuthSession.login(new User(USER_ID, "andi", "hash", "Andi", Role.USER, null));
        UI.getCurrent().add(new FinanceView(
                useCase, new FinancePresenter(), new FileStorage(tempDir.toString()), aiUseCase));
    }

    @AfterEach
    void tearDown() {
        AuthSession.logout();
        MockVaadin.tearDown();
    }

    @Test
    void addIncomeAndExpenseUpdatesSummaryAndGrid() throws Exception {
        assertTrue(_get(Paragraph.class).getText().contains("Data transaksi belum tersedia"));

        addViaDialog("Pemasukan", "Gaji", "5000000");
        addViaDialog("Pengeluaran", "Makan", "25000");

        assertEquals(2, useCase.getTransactionsByUser(USER_ID).size());
        assertEquals(List.of("2", "Rp 5,000,000.00", "Rp 25,000.00", "Rp 4,975,000.00"), summaryValues());
        assertEquals("Daftar Transaksi · Terbaru", _get(Paragraph.class).getText());

        Grid<Transaction> grid = grid();
        assertEquals(2, grid.getListDataView().getItemCount());
        assertEquals("Makan", grid.getListDataView().getItems().toList().get(0).getDescription());
    }

    @Test
    void invalidAmountKeepsDialogOpenAndSavesNothing() {
        clickButton("Pemasukan");
        Dialog dialog = _get(Dialog.class);
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Keterangan")), "Gaji");
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah")), "abc");
        clickDialogButton(dialog, "Simpan");
        assertTrue(useCase.getTransactionsByUser(USER_ID).isEmpty());
        clickDialogButton(dialog, "Batal");
    }

    @Test
    void searchEditAndDeleteFlow() throws Exception {
        useCase.addTransaction(USER_ID, "Gaji Bulanan", "1000", TransactionType.INCOME);
        useCase.addTransaction(USER_ID, "Makan siang", "50", TransactionType.EXPENSE);
        clickButton("Muat Ulang");

        TextField search = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        assertEquals("Cari transaksi", search.getLabel());
        _setValue(search, "tidakada");
        clickButton("Cari");
        assertTrue(_get(Paragraph.class).getText().contains("Transaksi tidak ditemukan"));

        _setValue(search, "makan");
        clickButton("Cari");
        assertEquals(1, grid().getListDataView().getItemCount());

        clickButton("Muat Ulang");
        Grid<Transaction> grid = grid();
        assertEquals(2, grid.getListDataView().getItemCount());
        assertEquals("Daftar transaksi", grid.getElement().getAttribute("aria-label"));
        HorizontalLayout editActions = (HorizontalLayout) _getCellComponent(grid, 0, "tindakan");
        _get(editActions, Button.class, spec -> spec.withCaption("Ubah"));

        _doubleClickItem(grid, 0);
        Dialog editDialog = _get(Dialog.class);
        _setValue(_get(editDialog, TextField.class, spec -> spec.withLabel("Keterangan")), "Makan malam");
        clickDialogButton(editDialog, "Simpan");
        assertTrue(useCase.getTransactionsByUser(USER_ID).stream()
                .anyMatch(tx -> tx.getDescription().equals("Makan malam")));

        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, 0, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        Dialog deleteDialog = _get(Dialog.class);
        clickDialogButton(deleteDialog, "Hapus");
        assertEquals(1, useCase.getTransactionsByUser(USER_ID).size());
        assertEquals("1", summaryValues().get(0));
    }

    @Test
    void aiDialogRejectsBlankPromptAndCanBeCancelled() {
        clickButton("Generate AI");
        Dialog dialog = _get(Dialog.class);
        clickDialogButton(dialog, "Proses AI");
        clickDialogButton(dialog, "Batal");
        assertTrue(useCase.getTransactionsByUser(USER_ID).isEmpty());
    }

    private void addViaDialog(String buttonCaption, String description, String amount) {
        clickButton(buttonCaption);
        Dialog dialog = _get(Dialog.class);
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Keterangan")), description);
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah")), amount);
        clickDialogButton(dialog, "Simpan");
    }

    @SuppressWarnings("unchecked")
    private Grid<Transaction> grid() {
        return _get(Grid.class);
    }

    /** Nilai ringkasan: [total transaksi, pemasukan, pengeluaran, saldo]. */
    private List<String> summaryValues() {
        FinanceView view = _get(FinanceView.class);
        HorizontalLayout summary = findFirst(view, HorizontalLayout.class);
        return summary.getChildren()
                .map(card -> ((Div) card).getChildren()
                        .skip(1)
                        .map(component -> ((Span) component).getText())
                        .findFirst()
                        .orElse(""))
                .toList();
    }

    private <T extends Component> T findFirst(Component root, Class<T> type) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        return root.getChildren()
                .map(child -> findFirstOrNull(child, type))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow();
    }

    private <T extends Component> T findFirstOrNull(Component root, Class<T> type) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        return root.getChildren()
                .map(child -> findFirstOrNull(child, type))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private void clickButton(String caption) {
        _click(_get(Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickDialogButton(Dialog dialog, String caption) {
        _click(_get(dialog, Button.class, spec -> spec.withCaption(caption)));
    }
}
