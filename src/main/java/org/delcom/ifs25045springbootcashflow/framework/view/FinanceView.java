package org.delcom.ifs25045springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25045springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25045springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25045springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25045springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25045springbootcashflow.domain.entity.User;
import org.delcom.ifs25045springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25045springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25045springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25045springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25045springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25045springbootcashflow.usecase.FinanceUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dashboard catatan keuangan milik pengguna yang sedang login.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private static final String EMPTY_ALL = "- Data transaksi belum tersedia!";
    private static final String EMPTY_FILTERED = "- Transaksi tidak ditemukan!";
    private static final String COLOR_INCOME = "#166534";
    private static final String COLOR_EXPENSE = "#991B1B";

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;
    private final FinanceAiUseCase financeAiUseCase;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Select<TransactionTypeFilter> typeFilter = new Select<>();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(EMPTY_ALL);
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span("Rp 0.00");
    private final Span expenseTotal = new Span("Rp 0.00");
    private final Span balanceTotal = new Span("Rp 0.00");

    public FinanceView(
            FinanceUseCase financeUseCase,
            FinancePresenter presenter,
            FileStorage fileStorage,
            FinanceAiUseCase financeAiUseCase
    ) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        this.financeAiUseCase = financeAiUseCase;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background", "#F3F4F6")
                .set("padding", "0.75rem")
                .set("min-height", "0")
                .set("box-sizing", "border-box");

        H1 title = new H1("Cashflow Saya");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.45rem")
                .set("line-height", "1.2");

        statusLabel.getStyle()
                .set("margin", "0")
                .set("font-size", "0.875rem")
                .set("color", "#4B5563");

        configureGrid();
        configureToolbar();

        Div card = PageCard.wrap(title, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        add(card);
        expand(card);
        card.getStyle().set("min-height", "0");
        refreshAll();
    }

    private User requireUser() {
        return AuthSession.currentUser().orElseThrow();
    }

    // ------------------------------------------------------------------ ringkasan

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createSummaryCard("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A", COLOR_INCOME),
                createSummaryCard("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626", COLOR_EXPENSE),
                createSummaryCard("Saldo", balanceTotal, "#FEF3C7", "#D97706", "#92400E")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        return summary;
    }

    private Div createSummaryCard(String label, Span value, String background, String accent, String valueColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.8rem")
                .set("font-weight", "500")
                .set("color", accent);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.15rem")
                .set("font-size", "1.2rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "11rem")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "4px solid " + accent)
                .set("border-radius", "10px")
                .set("padding", "0.55rem 0.85rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        FinanceUseCase.Balance balance = financeUseCase.getBalance(requireUser().getId());
        totalCount.setText(String.valueOf(balance.count()));
        incomeTotal.setText(presenter.formatAmount(balance.totalIncome()));
        expenseTotal.setText(presenter.formatAmount(balance.totalExpense()));
        balanceTotal.setText(presenter.formatAmount(balance.balance()));
    }

    // ------------------------------------------------------------------ grid & toolbar

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addComponentColumn(this::createAmountLabel)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparingLong(Transaction::getAmount));

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addComponentColumn(this::createProofPreview)
                .setHeader("Bukti")
                .setKey("proof")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.setSizeFull();
        GridStyles.apply(grid);
    }

    private Span createAmountLabel(Transaction transaction) {
        boolean income = transaction.getType() == TransactionType.INCOME;
        Span label = new Span((income ? "+ " : "- ") + presenter.formatAmount(transaction.getAmount()));
        label.getStyle()
                .set("font-weight", "600")
                .set("white-space", "nowrap")
                .set("color", income ? COLOR_INCOME : COLOR_EXPENSE);
        return label;
    }

    private Span createTypeBadge(Transaction transaction) {
        boolean income = transaction.getType() == TransactionType.INCOME;
        Span badge = new Span(transaction.getType().label());
        badge.getStyle()
                .set("padding", "0.1rem 0.55rem")
                .set("border-radius", "999px")
                .set("font-size", "0.78rem")
                .set("font-weight", "600")
                .set("background", income ? "#DCFCE7" : "#FEE2E2")
                .set("color", income ? COLOR_INCOME : COLOR_EXPENSE);
        return badge;
    }

    private Component createProofPreview(Transaction transaction) {
        if (transaction.getProofPath() == null || transaction.getProofPath().isBlank()) {
            return new Span("-");
        }
        return ImagePreviewDialog.clickableThumbnail(
                "/uploads/" + transaction.getProofPath(),
                "Bukti transaksi",
                "48px"
        );
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.getStyle().set("cursor", "pointer");
        return new HorizontalLayout(deleteButton);
    }

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.addKeyPressListener(Key.ENTER, event -> applyFilters());
        searchField.addValueChangeListener(event -> {
            if (event.isFromClient() && (event.getValue() == null || event.getValue().isBlank())) {
                applyFilters();
            }
        });

        typeFilter.setItems(TransactionTypeFilter.values());
        typeFilter.setItemLabelGenerator(TransactionTypeFilter::label);
        typeFilter.setValue(TransactionTypeFilter.ALL);
        typeFilter.setWidth("11rem");
        typeFilter.setAriaLabel("Filter tipe");
        typeFilter.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                applyFilters();
            }
        });

        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(SortOption::label);
        sortSelect.setValue(SortOption.NEWEST_FIRST);
        sortSelect.setWidth("12rem");
        sortSelect.setAriaLabel("Urutan");
        sortSelect.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                applyFilters();
            }
        });
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> applyFilters());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        Button aiButton = new Button("Generate AI", VaadinIcon.MAGIC.create(), event -> openAiDialog());
        aiButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button incomeButton = new Button("Pemasukan", VaadinIcon.PLUS.create(),
                event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

        Button expenseButton = new Button("Pengeluaran", VaadinIcon.MINUS.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        for (Button button : List.of(searchButton, refreshButton, aiButton, incomeButton, expenseButton)) {
            button.getStyle().set("cursor", "pointer");
        }

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField,
                typeFilter,
                sortSelect,
                searchButton,
                refreshButton,
                aiButton,
                incomeButton,
                expenseButton
        );
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    // ------------------------------------------------------------------ dialog AI

    private void openAiDialog() {
        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Span hint = new Span("Tulis catatan keuangan bebas, AI akan menyusunnya menjadi daftar transaksi:");
        hint.getStyle().set("font-size", "0.875rem").set("color", "#4B5563");

        TextArea promptArea = new TextArea("Catatan Keuangan");
        promptArea.setPlaceholder("Contoh: Gajian 5 juta, bayar kos 1,2 juta, beli makan siang 25 ribu...");
        promptArea.setWidthFull();
        promptArea.setMinHeight("100px");

        Grid<AiGeneratedTransaction> previewGrid = new Grid<>(AiGeneratedTransaction.class, false);
        previewGrid.addColumn(AiGeneratedTransaction::description).setHeader("Keterangan").setFlexGrow(2);
        previewGrid.addColumn(item -> presenter.formatAmount(item.amount())).setHeader("Jumlah").setFlexGrow(1);
        previewGrid.addColumn(item -> item.type().label()).setHeader("Tipe").setFlexGrow(1);
        previewGrid.setWidthFull();
        previewGrid.setHeight("200px");
        previewGrid.setVisible(false);
        GridStyles.apply(previewGrid);

        List<AiGeneratedTransaction> generatedItems = new ArrayList<>();

        Button applyButton = new Button("Tambahkan ke Catatan", VaadinIcon.PLUS_CIRCLE.create());
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyButton.setEnabled(false);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        Button generateButton = new Button("Proses AI", VaadinIcon.MAGIC.create());
        generateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        generateButton.getStyle().set("cursor", "pointer");

        generateButton.addClickListener(event -> {
            String promptText = promptArea.getValue();
            if (promptText == null || promptText.trim().isEmpty()) {
                showError("Prompt catatan keuangan tidak boleh kosong");
                return;
            }

            UI ui = UI.getCurrent();
            setAiBusy(true, loadingBar, generateButton, applyButton, cancelButton, promptArea);
            ui.setPollInterval(400);

            String prompt = promptText.trim();
            // Dijalankan async agar ProgressBar sempat tampil dan UI thread tidak terblokir.
            CompletableFuture.supplyAsync(() -> {
                try {
                    return financeAiUseCase.generateTransactions(prompt);
                } catch (DomainException ex) {
                    throw new CompletionException(ex.getMessage(), ex);
                }
            }).whenComplete((result, error) -> ui.access(() -> {
                try {
                    if (error != null) {
                        Throwable cause = error.getCause() != null ? error.getCause() : error;
                        showError("Gagal menghasilkan transaksi AI: " + cause.getMessage());
                        return;
                    }
                    generatedItems.clear();
                    generatedItems.addAll(result);
                    previewGrid.setItems(generatedItems);
                    previewGrid.setVisible(true);
                    showSuccess("AI berhasil menghasilkan " + generatedItems.size() + " transaksi");
                } finally {
                    setAiBusy(false, loadingBar, generateButton, applyButton, cancelButton, promptArea);
                    applyButton.setEnabled(!generatedItems.isEmpty());
                    ui.setPollInterval(-1);
                }
            }));
        });

        applyButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            applyButton.setEnabled(false);
            generateButton.setEnabled(false);
            cancelButton.setEnabled(false);
            try {
                financeAiUseCase.saveGeneratedTransactions(requireUser().getId(), generatedItems);
                showSuccess("Berhasil menambahkan " + generatedItems.size() + " transaksi dari AI");
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
                loadingBar.setVisible(false);
                applyButton.setEnabled(true);
                generateButton.setEnabled(true);
                cancelButton.setEnabled(true);
            }
        });

        HorizontalLayout promptActionRow = new HorizontalLayout(generateButton);
        promptActionRow.setWidthFull();
        promptActionRow.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout body = new VerticalLayout(hint, promptArea, promptActionRow, previewGrid);
        body.setPadding(false);
        body.setSpacing(true);

        buildDialog(dialog, "Generate Transaksi dengan AI", DialogTone.PRIMARY, loadingBar, body,
                cancelButton, applyButton);
        dialog.setWidth("620px");
        dialog.open();
    }

    // ------------------------------------------------------------------ dialog tambah / ubah / hapus

    private TextField amountField() {
        TextField field = new TextField("Jumlah");
        field.setPlaceholder("Contoh: 150000");
        field.setPrefixComponent(new Span("Rp"));
        field.setWidthFull();
        return field;
    }

    private void openAddDialog(TransactionType type) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        TextField amountField = amountField();

        AtomicReference<String> pendingProofName = new AtomicReference<>();
        AtomicReference<byte[]> pendingProofData = new AtomicReference<>();
        Upload upload = imageUpload(pendingProofName, pendingProofData);
        Span uploadHint = new Span("Bukti transaksi (opsional)");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                Transaction created = financeUseCase.addTransaction(
                        user.getId(), descriptionField.getValue(), amountField.getValue(), type);
                String filename = pendingProofName.get();
                byte[] data = pendingProofData.get();
                if (filename != null && !filename.isBlank() && data != null) {
                    String path = fileStorage.store("proofs", filename, new ByteArrayInputStream(data));
                    financeUseCase.updateTransactionForUser(
                            user.getId(), created.getId(), null, null, null, path, false);
                }
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException | EntityNotFoundException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(descriptionField, amountField, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);
        buildDialog(dialog, "Tambah " + type.label(), DialogTone.PRIMARY, loadingBar, body, cancelButton, saveButton);
        dialog.open();
    }

    private void openEditDialog(Transaction transaction) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setValue(transaction.getDescription());
        descriptionField.setWidthFull();
        TextField amountField = amountField();
        amountField.setValue(String.valueOf(transaction.getAmount()));

        Select<TransactionType> typeSelect = new Select<>();
        typeSelect.setLabel("Tipe");
        typeSelect.setItems(TransactionType.values());
        typeSelect.setItemLabelGenerator(TransactionType::label);
        typeSelect.setValue(transaction.getType());
        typeSelect.setWidthFull();

        AtomicReference<String> pendingProofName = new AtomicReference<>();
        AtomicReference<byte[]> pendingProofData = new AtomicReference<>();
        Upload upload = imageUpload(pendingProofName, pendingProofData);
        Span uploadHint = new Span("Ganti bukti transaksi (opsional)");
        Checkbox clearProof = new Checkbox("Hapus bukti saat ini");
        clearProof.setVisible(transaction.getProofPath() != null && !transaction.getProofPath().isBlank());

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                String proofPath = null;
                String filename = pendingProofName.get();
                byte[] data = pendingProofData.get();
                if (filename != null && !filename.isBlank() && data != null) {
                    proofPath = fileStorage.store("proofs", filename, new ByteArrayInputStream(data));
                }
                financeUseCase.updateTransactionForUser(
                        requireUser().getId(),
                        transaction.getId(),
                        descriptionField.getValue(),
                        amountField.getValue(),
                        typeSelect.getValue(),
                        proofPath,
                        clearProof.getValue()
                );
                showSuccess(presenter.showUpdateSuccess());
                dialog.close();
                refreshAll();
            } catch (EntityNotFoundException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showUpdateFailed(ex.getId()));
            } catch (ValidationException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(
                descriptionField, amountField, typeSelect, uploadHint, upload, clearProof);
        body.setPadding(false);
        body.setSpacing(true);
        buildDialog(dialog, "Mengubah Transaksi #" + transaction.getId(), DialogTone.PRIMARY, loadingBar, body,
                cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription() + "\" ("
                        + presenter.formatAmount(transaction.getAmount()) + ", ID " + transaction.getId() + ")?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            confirmButton.setEnabled(false);
            deleteTransaction(transaction);
            dialog.close();
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, "Menghapus Transaksi", DialogTone.DANGER, loadingBar, message, cancelButton, confirmButton);
        dialog.open();
    }

    private enum DialogTone {
        PRIMARY,
        DANGER
    }

    /** Dialog seragam: header berwarna, ProgressBar, isi, dan footer tombol rata kanan. */
    private void buildDialog(
            Dialog dialog,
            String title,
            DialogTone tone,
            ProgressBar loadingBar,
            Component bodyContent,
            Button... footerButtons
    ) {
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.addClassName("finance-form-dialog");

        String headerBackground = tone == DialogTone.DANGER ? "#DC2626" : "#2563EB";

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background", headerBackground)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        loadingBar.setIndeterminate(true);
        loadingBar.getStyle().set("margin", "0").set("height", "4px");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("overflow", "hidden")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        for (Button button : footerButtons) {
            button.getStyle().set("cursor", "pointer");
        }
        footer.add(footerButtons);

        Div shell = new Div(header, loadingBar, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
        removeOverlayPadding(dialog);
    }

    private void removeOverlayPadding(Dialog dialog) {
        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs("""
                        const overlay = this.$.overlay;
                        if (!overlay || !overlay.shadowRoot) {
                          return;
                        }
                        const content = overlay.shadowRoot.querySelector('[part="content"]');
                        if (content) {
                          content.style.padding = '0';
                          content.style.overflow = 'visible';
                          content.style.maxHeight = 'none';
                        }
                        const overlayPart = overlay.shadowRoot.querySelector('[part="overlay"]');
                        if (overlayPart) {
                          overlayPart.style.padding = '0';
                          overlayPart.style.overflow = 'hidden';
                        }
                        """);
            }
        });
    }

    // ------------------------------------------------------------------ aksi & penyegaran data

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransactionForUser(requireUser().getId(), transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void applyFilters() {
        String keyword = searchField.getValue() == null ? "" : searchField.getValue().trim();
        TransactionTypeFilter type = typeFilter.getValue() == null ? TransactionTypeFilter.ALL : typeFilter.getValue();
        SortOption sort = sortSelect.getValue() == null ? SortOption.NEWEST_FIRST : sortSelect.getValue();

        List<Transaction> results = financeUseCase.listTransactions(requireUser().getId(), keyword, type, sort);

        String title = "Daftar Transaksi";
        if (!keyword.isBlank()) {
            title = "Hasil: \"" + keyword + "\"";
        }
        if (type != TransactionTypeFilter.ALL) {
            title = title + " · " + type.label();
        }
        title = title + " · " + sort.label();

        emptyState.setText(keyword.isBlank() && type == TransactionTypeFilter.ALL ? EMPTY_ALL : EMPTY_FILTERED);
        refreshGrid(results, title);
    }

    private void refreshAll() {
        searchField.clear();
        typeFilter.setValue(TransactionTypeFilter.ALL);
        sortSelect.setValue(SortOption.NEWEST_FIRST);
        emptyState.setText(EMPTY_ALL);
        applyFilters();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title);
        }
    }

    private Upload imageUpload(AtomicReference<String> pendingName, AtomicReference<byte[]> pendingData) {
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pendingName.set(metadata.fileName());
            pendingData.set(data);
        }));
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/webp");
        upload.setAcceptedFileExtensions(".png", ".jpg", ".jpeg", ".webp");
        upload.setMaxFiles(1);
        return upload;
    }

    private void setAiBusy(
            boolean busy,
            ProgressBar loadingBar,
            Button generateButton,
            Button applyButton,
            Button cancelButton,
            TextArea promptArea
    ) {
        loadingBar.setVisible(busy);
        generateButton.setEnabled(!busy);
        generateButton.setText(busy ? "Memproses..." : "Proses AI");
        applyButton.setEnabled(false);
        cancelButton.setEnabled(!busy);
        promptArea.setReadOnly(busy);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
