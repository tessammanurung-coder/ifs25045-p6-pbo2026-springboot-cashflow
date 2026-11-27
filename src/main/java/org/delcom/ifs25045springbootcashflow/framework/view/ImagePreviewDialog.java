package org.delcom.ifs25045springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Modal untuk menampilkan gambar dalam ukuran diperbesar dengan fitur zoom interaktif.
 */
final class ImagePreviewDialog {
    private static final String DIALOG_OVERLAY_JS = """
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
            """;

    private static final String STYLE_ALIGN_ITEMS = "align-items";
    private static final String STYLE_BACKGROUND = "background";
    private static final String STYLE_BLOCK = "block";
    private static final String STYLE_BORDER_BOX = "border-box";
    private static final String STYLE_BORDER_RADIUS = "border-radius";
    private static final String STYLE_BOX_SIZING = "box-sizing";
    private static final String STYLE_CENTER = "center";
    private static final String STYLE_CURSOR = "cursor";
    private static final String STYLE_DISPLAY = "display";
    private static final String STYLE_HIDDEN = "hidden";
    private static final String STYLE_JUSTIFY_CONTENT = "justify-content";
    private static final String STYLE_MAX_WIDTH = "max-width";
    private static final String STYLE_OVERFLOW = "overflow";
    private static final String STYLE_PADDING = "padding";
    private static final String STYLE_POINTER = "pointer";
    private static final String STYLE_WIDTH = "width";


    private ImagePreviewDialog() {
    }

    static void open(String imageUrl, String alt) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String title = alt == null || alt.isBlank() ? "Pratinjau Bukti" : alt;

        Span headerHint = new Span("Klik gambar untuk memperbesar / memperkecil");
        headerHint.getStyle()
                .set("margin", "0")
                .set("color", "#475569")
                .set("font-size", "0.8rem")
                .set(STYLE_DISPLAY, STYLE_BLOCK);

        Image image = new Image(imageUrl, title);
        image.getStyle()
                .set(STYLE_DISPLAY, STYLE_BLOCK)
                .set(STYLE_MAX_WIDTH, "100%")
                .set("max-height", "68vh")
                .set(STYLE_WIDTH, "auto")
                .set("height", "auto")
                .set("object-fit", "contain")
                .set(STYLE_BORDER_RADIUS, "8px")
                .set(STYLE_CURSOR, "zoom-in")
                .set("transition", "transform 0.25s ease")
                .set("user-select", "none");
        image.getElement().setAttribute("title", "Klik untuk zoom");

        Button imageZoomButton = new Button(image);
        imageZoomButton.setAriaLabel("Perbesar atau perkecil gambar: " + title);
        imageZoomButton.getStyle()
                .set(STYLE_PADDING, "0")
                .set("border", "0")
                .set(STYLE_BACKGROUND, "transparent")
                .set("box-shadow", "none")
                .set(STYLE_MAX_WIDTH, "100%");

        Div imageWrapper = new Div(imageZoomButton);
        imageWrapper.getStyle()
                .set(STYLE_DISPLAY, "flex")
                .set(STYLE_JUSTIFY_CONTENT, STYLE_CENTER)
                .set(STYLE_ALIGN_ITEMS, STYLE_CENTER)
                .set(STYLE_OVERFLOW, "auto")
                .set("max-height", "70vh")
                .set(STYLE_MAX_WIDTH, "min(88vw, 860px)")
                .set(STYLE_WIDTH, "100%")
                .set(STYLE_BOX_SIZING, STYLE_BORDER_BOX);

        Div body = new Div(headerHint, imageWrapper);
        body.getStyle()
                .set(STYLE_DISPLAY, "flex")
                .set("flex-direction", "column")
                .set(STYLE_JUSTIFY_CONTENT, STYLE_CENTER)
                .set(STYLE_ALIGN_ITEMS, STYLE_CENTER)
                .set("gap", "0.75rem")
                .set(STYLE_BACKGROUND, "#FFFFFF")
                .set(STYLE_PADDING, "1rem 1.25rem")
                .set(STYLE_BOX_SIZING, STYLE_BORDER_BOX)
                .set(STYLE_WIDTH, "100%")
                .set(STYLE_OVERFLOW, STYLE_HIDDEN);

        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setResizable(false);
        dialog.setMaxWidth("95vw");
        dialog.setHeaderTitle(title);

        AtomicReference<Double> currentScale = new AtomicReference<>(1.0);

        Runnable applyScale = () -> {
            double scale = currentScale.get();
            if (scale <= 1.0) {
                currentScale.set(1.0);
                image.getStyle()
                        .set("transform", "scale(1)")
                        .set(STYLE_CURSOR, "zoom-in");
            } else {
                image.getStyle()
                        .set("transform", "scale(" + scale + ")")
                        .set("transform-origin", "center center")
                        .set(STYLE_CURSOR, "zoom-out");
            }
        };

        // Klik langsung pada gambar untuk toggle zoom
        imageZoomButton.addClickListener(event -> {
            if (currentScale.get() > 1.0) {
                currentScale.set(1.0);
            } else {
                currentScale.set(1.8);
            }
            applyScale.run();
        });

        Button zoomOutButton = new Button("Perkecil", VaadinIcon.SEARCH_MINUS.create(), event -> {
            currentScale.set(Math.max(currentScale.get() - 0.4, 1.0));
            applyScale.run();
        });
        zoomOutButton.getStyle().set(STYLE_CURSOR, STYLE_POINTER);

        Button zoomInButton = new Button("Perbesar", VaadinIcon.SEARCH_PLUS.create(), event -> {
            currentScale.set(Math.min(currentScale.get() + 0.4, 3.0));
            applyScale.run();
        });
        zoomInButton.getStyle().set(STYLE_CURSOR, STYLE_POINTER);

        Button closeButton = new Button("Tutup", VaadinIcon.CLOSE.create(), event -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.getStyle().set(STYLE_CURSOR, STYLE_POINTER);

        Div footer = new Div(zoomOutButton, zoomInButton, closeButton);
        footer.addClassName("dialog-footer");
        footer.getStyle()
                .set(STYLE_DISPLAY, "flex")
                .set(STYLE_JUSTIFY_CONTENT, "flex-end")
                .set("flex-wrap", "wrap")
                .set(STYLE_ALIGN_ITEMS, STYLE_CENTER)
                .set("gap", "0.6rem")
                .set(STYLE_BACKGROUND, "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set(STYLE_PADDING, "0.85rem 1.5rem 1.1rem")
                .set(STYLE_BOX_SIZING, STYLE_BORDER_BOX)
                .set(STYLE_WIDTH, "100%");

        Div shell = new Div(body, footer);
        shell.getStyle()
                .set(STYLE_DISPLAY, "flex")
                .set("flex-direction", "column")
                .set(STYLE_WIDTH, "100%")
                .set(STYLE_OVERFLOW, STYLE_HIDDEN)
                .set(STYLE_BORDER_RADIUS, "12px")
                .set(STYLE_BACKGROUND, "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);

        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs(DIALOG_OVERLAY_JS);
            }
        });

        dialog.open();
    }

    static Button clickableThumbnail(String imageUrl, String alt, String height) {
        return clickableThumbnail(imageUrl, alt, height, "6px");
    }

    static Button clickableThumbnail(String imageUrl, String alt, String height, String borderRadius) {
        Image image = new Image(imageUrl, alt);
        image.setWidth(height);
        image.setHeight(height);
        image.getStyle()
                .set(STYLE_BORDER_RADIUS, borderRadius)
                .set("object-fit", "cover")
                .set(STYLE_DISPLAY, STYLE_BLOCK);
        Button previewButton = new Button(image, event -> open(imageUrl, alt));
        previewButton.setAriaLabel("Perbesar gambar: " + alt);
        previewButton.getElement().setAttribute("title", "Klik untuk memperbesar");
        previewButton.getStyle()
                .set(STYLE_PADDING, "0")
                .set("min-width", height)
                .set(STYLE_WIDTH, height)
                .set("height", height)
                .set(STYLE_BORDER_RADIUS, borderRadius)
                .set(STYLE_OVERFLOW, STYLE_HIDDEN);
        return previewButton;
    }
}
