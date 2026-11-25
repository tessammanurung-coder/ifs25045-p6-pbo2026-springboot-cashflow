package org.delcom.ifs25045springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Card konten halaman agar tampilan konsisten.
 * Komponen terakhir (biasanya grid) mengisi sisa tinggi agar tabel tidak terdesak.
 */
final class PageCard {

    private PageCard() {
    }

    static Div wrap(Component... content) {
        return wrap(true, content);
    }

    static Div wrap(boolean fullHeight, Component... content) {
        VerticalLayout body = new VerticalLayout();
        body.setPadding(false);
        body.setSpacing(false);
        body.setWidthFull();
        body.getStyle().set("gap", "0.65rem");

        for (Component component : content) {
            body.add(component);
        }

        if (fullHeight) {
            body.setSizeFull();
            body.getStyle().set("min-height", "0").set("flex", "1 1 auto");
            if (content.length > 0) {
                Component last = content[content.length - 1];
                body.setFlexGrow(1, last);
                last.getElement().getStyle().set("min-height", "0");
            }
        }

        Div card = new Div(body);
        card.setWidthFull();
        if (fullHeight) {
            card.setHeightFull();
            card.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("min-height", "0")
                    .set("overflow", "hidden");
        }
        card.getStyle()
                .set("background", "#FFFFFF")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-shadow", "0 8px 24px rgba(15, 23, 42, 0.06)")
                .set("box-sizing", "border-box");
        return card;
    }
}
