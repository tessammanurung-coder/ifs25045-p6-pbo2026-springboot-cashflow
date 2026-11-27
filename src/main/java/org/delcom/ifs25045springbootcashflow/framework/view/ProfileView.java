package org.delcom.ifs25045springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25045springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25045springbootcashflow.domain.entity.User;
import org.delcom.ifs25045springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25045springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25045springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25045springbootcashflow.usecase.UserUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil")
public class ProfileView extends VerticalLayout {
    private static final String STYLE_BACKGROUND = "background";
    private static final String STYLE_BORDER_RADIUS = "border-radius";
    private static final String STYLE_COLOR = "color";
    private static final String STYLE_HEIGHT = "height";
    private static final String STYLE_WIDTH = "width";
    private static final String VALUE_120PX = "120px";


    private final UserUseCase userUseCase;
    private final FileStorage fileStorage;
    private final Image photoPreview = new Image();
    private final Button photoPreviewButton = new Button(photoPreview);
    private final Span photoInitial = new Span();
    private final Div photoFrame = new Div();
    private final Span photoPlaceholder = new Span("Belum ada foto profil. Upload foto di bawah.");

    public ProfileView(UserUseCase userUseCase, FileStorage fileStorage) {
        this.userUseCase = userUseCase;
        this.fileStorage = fileStorage;

        setSizeFull();
        getElement().setAttribute("role", "main");
        setPadding(true);
        setSpacing(false);
        getStyle().set(STYLE_BACKGROUND, "#F3F4F6");

        User user = AuthSession.currentUser().orElseThrow();

        TextField fullName = new TextField("Nama Lengkap");
        fullName.setWidthFull();
        fullName.setValue(user.getFullName());

        TextField username = new TextField("Username");
        username.setWidthFull();
        username.setValue(user.getUsername());
        username.setReadOnly(true);

        PasswordField password = new PasswordField("Password Baru (opsional)");
        password.setWidthFull();

        configurePhotoFrame();
        refreshPhoto(user);

        AtomicReference<String> pendingPhotoName = new AtomicReference<>();
        AtomicReference<byte[]> pendingPhotoData = new AtomicReference<>();

        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pendingPhotoName.set(metadata.fileName());
            pendingPhotoData.set(data);
        }));
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/webp");
        upload.setAcceptedFileExtensions(".png", ".jpg", ".jpeg", ".webp");
        upload.setMaxFiles(1);
        upload.setWidthFull();
        upload.getElement().setAttribute("aria-label", "Unggah foto profil");

        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setWidthFull();
        loadingBar.getElement().setAttribute("aria-label", "Sedang menyimpan profil");

        Button savePhoto = new Button("Simpan Foto");
        savePhoto.getStyle().set("cursor", "pointer");

        Button saveProfile = new Button("Simpan Profil");
        saveProfile.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveProfile.getStyle().set("margin-top", "0.5rem");
        saveProfile.getStyle().set("cursor", "pointer");

        savePhoto.addClickListener(event -> {
            loadingBar.setVisible(true);
            savePhoto.setEnabled(false);
            try {
                String filename = pendingPhotoName.get();
                byte[] data = pendingPhotoData.get();
                if (filename == null || filename.isBlank() || data == null) {
                    loadingBar.setVisible(false);
                    savePhoto.setEnabled(true);
                    showError("Pilih file foto terlebih dahulu");
                    return;
                }
                String path = fileStorage.store("profiles", filename, new ByteArrayInputStream(data));
                User updated = userUseCase.updatePhoto(user.getId(), path);
                AuthSession.refresh(updated);
                refreshPhoto(updated);
                pendingPhotoName.set(null);
                pendingPhotoData.set(null);
                loadingBar.setVisible(false);
                savePhoto.setEnabled(true);
                showSuccess("Foto profil diperbarui");
                UI.getCurrent().navigate(ProfileView.class);
            } catch (ValidationException | EntityNotFoundException | IOException ex) {
                loadingBar.setVisible(false);
                savePhoto.setEnabled(true);
                showError(ex.getMessage());
            }
        });

        saveProfile.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveProfile.setEnabled(false);
            try {
                User updated = userUseCase.updateProfile(user.getId(), fullName.getValue(), password.getValue());
                AuthSession.refresh(updated);
                password.clear();
                loadingBar.setVisible(false);
                saveProfile.setEnabled(true);
                showSuccess("Profil diperbarui");
            } catch (ValidationException | EntityNotFoundException ex) {
                loadingBar.setVisible(false);
                saveProfile.setEnabled(true);
                showError(ex.getMessage());
            }
        });

        H1 title = new H1("Profil");
        title.getStyle().set("margin", "0");
        Paragraph subtitle = new Paragraph("Perbarui nama, password, dan foto profil.");
        subtitle.getStyle().set("margin", "0").set(STYLE_COLOR, "#4B5563");

        Span photoLabel = new Span("Foto Profil");
        photoLabel.getStyle().set("font-weight", "600").set(STYLE_COLOR, "#1F2937");

        HorizontalLayout photoRow = new HorizontalLayout(photoFrame, photoPlaceholder);
        photoRow.setAlignItems(FlexComponent.Alignment.CENTER);
        photoRow.setSpacing(true);
        photoRow.setWidthFull();

        Div card = PageCard.wrap(
                false,
                title,
                subtitle,
                photoLabel,
                photoRow,
                upload,
                savePhoto,
                fullName,
                username,
                password,
                loadingBar,
                saveProfile);
        card.getStyle().set("max-width", "560px");
        add(card);
    }

    private void configurePhotoFrame() {
        photoPreview.setWidth(VALUE_120PX);
        photoPreview.setHeight(VALUE_120PX);
        photoPreview.getStyle()
                .set(STYLE_BORDER_RADIUS, "50%")
                .set("object-fit", "cover")
                .set("display", "block");
        photoPreviewButton.setAriaLabel("Perbesar foto profil");
        photoPreviewButton.getElement().setAttribute("title", "Klik untuk memperbesar");
        photoPreviewButton.getStyle()
                .set("padding", "0")
                .set("min-width", VALUE_120PX)
                .set(STYLE_WIDTH, VALUE_120PX)
                .set(STYLE_HEIGHT, VALUE_120PX)
                .set(STYLE_BORDER_RADIUS, "50%");
        photoPreviewButton.addClickListener(
                event -> ImagePreviewDialog.open(photoPreview.getSrc(), photoPreview.getAlt().orElse("Foto profil")));

        photoInitial.getStyle()
                .set(STYLE_WIDTH, VALUE_120PX)
                .set(STYLE_HEIGHT, VALUE_120PX)
                .set(STYLE_BORDER_RADIUS, "50%")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set(STYLE_BACKGROUND, "#DBEAFE")
                .set(STYLE_COLOR, "#1D4ED8")
                .set("font-weight", "700")
                .set("font-size", "2.5rem");

        photoFrame.getStyle()
                .set(STYLE_WIDTH, VALUE_120PX)
                .set(STYLE_HEIGHT, VALUE_120PX)
                .set(STYLE_BORDER_RADIUS, "50%")
                .set("overflow", "hidden")
                .set("border", "3px solid #93C5FD")
                .set("flex-shrink", "0")
                .set(STYLE_BACKGROUND, "#EFF6FF");

        photoPlaceholder.getStyle().set(STYLE_COLOR, "#6B7280");
    }

    private void refreshPhoto(User user) {
        String initial = user.getFullName() == null || user.getFullName().isBlank()
                ? user.getUsername().substring(0, 1).toUpperCase()
                : user.getFullName().substring(0, 1).toUpperCase();
        photoInitial.setText(initial);

        photoFrame.removeAll();
        if (user.getPhotoPath() == null || user.getPhotoPath().isBlank()) {
            photoFrame.add(photoInitial);
            photoPlaceholder.setVisible(true);
            return;
        }
        photoPreview.setSrc("/uploads/" + user.getPhotoPath() + "?t=" + System.currentTimeMillis());
        photoPreview.setAlt(user.getFullName() == null ? "Foto profil" : user.getFullName());
        photoFrame.add(photoPreviewButton);
        photoPlaceholder.setVisible(false);
    }

    private void showSuccess(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification n = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
