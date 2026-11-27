package org.delcom.ifs25045springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.HasDynamicTitle;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Halaman 404 kustom. Berbeda dengan halaman bawaan Vaadin, halaman ini memiliki
 * judul dokumen, heading level-satu, dan landmark main (syarat aksesibilitas axe).
 */
public class NotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException>, HasDynamicTitle {

    private final Paragraph message = new Paragraph();

    public NotFoundView() {
        setSizeFull();
        getElement().setAttribute("role", "main");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "#F3F4F6").set("text-align", "center");

        H1 title = new H1("Halaman tidak ditemukan");
        title.getStyle().set("margin", "0");

        message.getStyle().set("margin", "0").set("color", "#4B5563");

        RouterLink home = new RouterLink("Kembali ke beranda", FinanceView.class);
        home.getStyle().set("color", "#1D4ED8").set("font-weight", "600").set("cursor", "pointer");

        add(title, message, home);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        message.setText("Alamat \"/" + event.getLocation().getPath() + "\" tidak tersedia di aplikasi Cashflow.");
        return HttpServletResponse.SC_NOT_FOUND;
    }

    @Override
    public String getPageTitle() {
        return "Halaman Tidak Ditemukan";
    }
}
