package org.delcom.ifs25045springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25045springbootcashflow.domain.entity.User;
import org.delcom.ifs25045springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25045springbootcashflow.framework.auth.AuthSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Layout utama untuk halaman yang memerlukan login.
 */
public class MainLayout extends AppLayout implements BeforeEnterObserver, AfterNavigationObserver {
    private static final String STYLE_BACKGROUND = "background";
    private static final String STYLE_BLOCK = "block";
    private static final String STYLE_BORDER = "border";
    private static final String STYLE_BORDER_RADIUS = "border-radius";
    private static final String STYLE_COLOR = "color";
    private static final String STYLE_CURSOR = "cursor";
    private static final String STYLE_DATA_ACTIVE = "data-active";
    private static final String STYLE_DISPLAY = "display";
    private static final String STYLE_FLEX_SHRINK = "flex-shrink";
    private static final String STYLE_FONT_SIZE = "font-size";
    private static final String STYLE_FONT_WEIGHT = "font-weight";
    private static final String STYLE_HEIGHT = "height";
    private static final String STYLE_MARGIN = "margin";
    private static final String STYLE_PADDING = "padding";
    private static final String STYLE_POINTER = "pointer";
    private static final String STYLE_WIDTH = "width";
    private static final String VALUE_0_95REM = "0.95rem";
    private static final String VALUE_1D4ED8 = "#1D4ED8";
    private static final String VALUE_1F2937 = "#1F2937";
    private static final String VALUE_1PX_SOLID_E5E7EB = "1px solid #E5E7EB";
    private static final String VALUE_4B5563 = "#4B5563";


    private final IUserRepository userRepository;
    private final List<NavEntry> navEntries = new ArrayList<>();
    private final Image headerAvatarImage = new Image();
    private final Span headerAvatarInitial = new Span();
    private final Div headerAvatar = new Div();
    private final Button headerAvatarButton = new Button();

    private record NavEntry(RouterLink link, String pathPrefix) {
    }

    public MainLayout(IUserRepository userRepository) {
        this.userRepository = userRepository;
        setPrimarySection(Section.DRAWER);
        AuthSession.currentUser(userRepository);
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        HorizontalLayout brandTitle = appBrandTitle("1.2rem", "28px");

        configureHeaderAvatar();
        refreshHeaderAvatar(AuthSession.currentUser().orElse(null));

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), brandTitle, headerAvatarButton);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(brandTitle);
        header.setWidthFull();
        header.setPadding(true);
        // Landmark banner agar seluruh isi header (logo, judul, avatar) berada di dalam landmark.
        header.getElement().setAttribute("role", "banner");
        header.getStyle()
                .set(STYLE_BACKGROUND, "#FFFFFF")
                .set("border-bottom", VALUE_1PX_SOLID_E5E7EB);
        addToNavbar(header);
    }

    private HorizontalLayout appBrandTitle(String fontSize, String iconSize) {
        Image logo = new Image("icons/icon.png", "Cashflow");
        logo.setWidth(iconSize);
        logo.setHeight(iconSize);
        logo.getStyle()
                .set(STYLE_DISPLAY, STYLE_BLOCK)
                .set(STYLE_BORDER_RADIUS, "6px")
                .set("object-fit", "cover")
                .set(STYLE_FLEX_SHRINK, "0");

        Span title = new Span("Cashflow");
        title.getStyle()
                .set(STYLE_FONT_SIZE, fontSize)
                .set(STYLE_FONT_WEIGHT, "700")
                .set(STYLE_MARGIN, "0")
                .set(STYLE_COLOR, "#111827")
                .set("line-height", "1.2");

        HorizontalLayout brand = new HorizontalLayout(logo, title);
        brand.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        brand.setSpacing(false);
        brand.setPadding(false);
        brand.getStyle().set("gap", "0.65rem");
        return brand;
    }

    private void configureHeaderAvatar() {
        headerAvatarImage.setWidth("40px");
        headerAvatarImage.setHeight("40px");
        headerAvatarImage.getStyle()
                .set(STYLE_BORDER_RADIUS, "50%")
                .set("object-fit", "cover")
                .set(STYLE_DISPLAY, STYLE_BLOCK);

        headerAvatarInitial.getStyle()
                .set(STYLE_WIDTH, "40px")
                .set(STYLE_HEIGHT, "40px")
                .set(STYLE_BORDER_RADIUS, "50%")
                .set(STYLE_DISPLAY, "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set(STYLE_BACKGROUND, "#DBEAFE")
                .set(STYLE_COLOR, VALUE_1D4ED8)
                .set(STYLE_FONT_WEIGHT, "700")
                .set(STYLE_FONT_SIZE, VALUE_0_95REM);

        headerAvatar.removeAll();
        headerAvatar.add(headerAvatarInitial);
        headerAvatar.getStyle()
                .set(STYLE_BORDER, "2px solid #93C5FD")
                .set(STYLE_BORDER_RADIUS, "50%")
                .set("overflow", "hidden")
                .set(STYLE_WIDTH, "40px")
                .set(STYLE_HEIGHT, "40px")
                .set(STYLE_FLEX_SHRINK, "0");
        headerAvatarButton.setIcon(headerAvatar);
        headerAvatarButton.setAriaLabel("Menu akun");
        headerAvatarButton.getElement().setAttribute("title", "Menu akun");
        headerAvatarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        headerAvatarButton.getStyle()
                .set(STYLE_PADDING, "0")
                .set("min-width", "40px")
                .set(STYLE_WIDTH, "40px")
                .set(STYLE_HEIGHT, "40px")
                .set(STYLE_BORDER_RADIUS, "50%");

        ContextMenu accountMenu = new ContextMenu(headerAvatarButton);
        accountMenu.setOpenOnClick(true);

        AuthSession.currentUser().ifPresent(user -> {
            var identityItem = accountMenu.addItem(accountIdentity(user));
            identityItem.setEnabled(false);
            identityItem.getStyle().set(STYLE_CURSOR, "default");
            var profileItem = accountMenu.addItem(menuItemContent(VaadinIcon.USER, "Profil", VALUE_4B5563, VALUE_1F2937), event ->
                    UI.getCurrent().navigate(ProfileView.class));
            profileItem.getStyle().set(STYLE_CURSOR, STYLE_POINTER);
            var logoutItem = accountMenu.addItem(menuItemContent(VaadinIcon.SIGN_OUT, "Keluar", "#DC2626", "#DC2626"), event -> {
                AuthSession.logout();
                // Full reload agar UI tidak blank setelah keluar dari MainLayout.
                UI.getCurrent().getPage().setLocation("login");
            });
            logoutItem.getStyle().set(STYLE_CURSOR, STYLE_POINTER);
        });
    }

    private VerticalLayout accountIdentity(User user) {
        Span username = new Span(user.getUsername());
        username.getStyle()
                .set(STYLE_DISPLAY, STYLE_BLOCK)
                .set(STYLE_FONT_WEIGHT, "700")
                .set(STYLE_FONT_SIZE, VALUE_0_95REM)
                .set(STYLE_COLOR, "#111827")
                .set(STYLE_MARGIN, "0");

        Span role = new Span(user.getRole().name());
        role.getStyle()
                .set(STYLE_DISPLAY, STYLE_BLOCK)
                .set("margin-top", "0.15rem")
                .set(STYLE_FONT_SIZE, "0.75rem")
                .set(STYLE_FONT_WEIGHT, "600")
                .set("letter-spacing", "0.02em")
                .set(STYLE_COLOR, VALUE_1D4ED8);

        VerticalLayout identity = new VerticalLayout(username, role);
        identity.setPadding(false);
        identity.setSpacing(false);
        identity.setMargin(false);
        identity.getStyle()
                .set("gap", "0")
                .set(STYLE_PADDING, "0.15rem 0")
                .set("min-width", "8rem");
        return identity;
    }

    private HorizontalLayout menuItemContent(VaadinIcon iconType, String label, String iconColor, String textColor) {
        Icon icon = iconType.create();
        icon.setSize("1rem");
        icon.getStyle()
                .set(STYLE_COLOR, iconColor)
                .set(STYLE_FLEX_SHRINK, "0")
                .set(STYLE_MARGIN, "0");

        Span text = new Span(label);
        text.getStyle()
                .set(STYLE_COLOR, textColor)
                .set(STYLE_FONT_WEIGHT, "600")
                .set(STYLE_MARGIN, "0");

        HorizontalLayout content = new HorizontalLayout(icon, text);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        content.setSpacing(false);
        content.setPadding(false);
        content.setMargin(false);
        content.setWidth(null);
        content.getStyle()
                .set("gap", "0.5rem")
                .set(STYLE_MARGIN, "0")
                .set(STYLE_PADDING, "0")
                .set("justify-content", "flex-start")
                .set(STYLE_DISPLAY, "inline-flex");
        return content;
    }

    private void refreshHeaderAvatar(User user) {
        if (user == null) {
            headerAvatarInitial.setText("?");
            headerAvatar.removeAll();
            headerAvatar.add(headerAvatarInitial);
            return;
        }

        String initial = user.getFullName() == null || user.getFullName().isBlank()
                ? user.getUsername().substring(0, 1).toUpperCase()
                : user.getFullName().substring(0, 1).toUpperCase();
        headerAvatarInitial.setText(initial);

        headerAvatar.removeAll();
        if (user.getPhotoPath() != null && !user.getPhotoPath().isBlank()) {
            headerAvatarImage.setSrc("/uploads/" + user.getPhotoPath() + "?t=" + System.currentTimeMillis());
            headerAvatarImage.setAlt(user.getFullName());
            headerAvatar.add(headerAvatarImage);
        } else {
            headerAvatar.add(headerAvatarInitial);
        }
    }

    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        drawer.setSizeFull();
        drawer.setPadding(false);
        drawer.setSpacing(false);
        // Landmark navigation untuk seluruh drawer (brand + menu), bukan hanya daftar menunya.
        drawer.getElement().setAttribute("role", "navigation");
        drawer.getElement().setAttribute("aria-label", "Navigasi utama");
        drawer.getStyle()
                .set(STYLE_BACKGROUND, "#FFFFFF")
                .set("border-right", VALUE_1PX_SOLID_E5E7EB);

        Div brand = new Div();
        brand.getStyle()
                .set(STYLE_PADDING, "1.25rem 1rem 1rem")
                .set("border-bottom", VALUE_1PX_SOLID_E5E7EB);
        brand.add(appBrandTitle("1.05rem", "30px"));

        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(true);
        menu.setSpacing(false);
        menu.getStyle().set("gap", "0.35rem").set("padding-top", "1rem");

        menu.add(navItem("Cashflow Saya", FinanceView.class, "", VaadinIcon.WALLET));
        menu.add(navItem("Profil", ProfileView.class, "profile", VaadinIcon.USER));

        AuthSession.currentUser()
                .filter(User::isAdmin)
                .ifPresent(user -> menu.add(navItem("Daftar Pengguna", UsersView.class, "users", VaadinIcon.USERS)));

        drawer.add(brand, menu);
        drawer.expand(menu);
        addToDrawer(drawer);
    }

    private RouterLink navItem(
            String label,
            Class<? extends Component> view,
            String pathPrefix,
            VaadinIcon iconType
    ) {
        Icon icon = iconType.create();
        icon.setSize("1.05rem");
        icon.getStyle().set(STYLE_COLOR, VALUE_4B5563);

        Span text = new Span(label);
        text.getStyle()
                .set(STYLE_FONT_SIZE, VALUE_0_95REM)
                .set(STYLE_FONT_WEIGHT, "500")
                .set(STYLE_COLOR, VALUE_1F2937);

        HorizontalLayout content = new HorizontalLayout(icon, text);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.setPadding(false);
        content.getStyle().set("gap", "0.65rem");

        RouterLink link = new RouterLink();
        link.setRoute(view);
        link.add(content);
        link.getStyle()
                .set(STYLE_DISPLAY, STYLE_BLOCK)
                .set(STYLE_WIDTH, "100%")
                .set("text-decoration", "none")
                .set(STYLE_PADDING, "0.7rem 0.85rem")
                .set(STYLE_BORDER_RADIUS, "10px")
                .set("box-sizing", "border-box")
                .set(STYLE_BORDER, "1px solid transparent")
                .set("transition", "background 0.15s ease")
                .set(STYLE_CURSOR, STYLE_POINTER);

        link.getElement().addEventListener("mouseover", e -> {
            if (!"true".equals(link.getElement().getAttribute(STYLE_DATA_ACTIVE))) {
                link.getStyle().set(STYLE_BACKGROUND, "#F3F4F6");
            }
        });
        link.getElement().addEventListener("mouseout", e -> {
            if (!"true".equals(link.getElement().getAttribute(STYLE_DATA_ACTIVE))) {
                link.getStyle().set(STYLE_BACKGROUND, "transparent");
            }
        });

        navEntries.add(new NavEntry(link, pathPrefix));
        return link;
    }

    private void highlightActive(String path) {
        String normalized = path == null ? "" : path;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        for (NavEntry entry : navEntries) {
            boolean active;
            if (entry.pathPrefix().isEmpty()) {
                active = normalized.isEmpty();
            } else {
                active = normalized.equals(entry.pathPrefix())
                        || normalized.startsWith(entry.pathPrefix() + "/");
            }
            // Highlight Daftar Pengguna juga saat melihat transaksi user lain.
            if ("users".equals(entry.pathPrefix()) && normalized.startsWith("user-transactions")) {
                active = true;
            }

            RouterLink link = entry.link();
            if (active) {
                link.getElement().setAttribute(STYLE_DATA_ACTIVE, "true");
                link.getElement().setAttribute("aria-current", "page");
                link.getStyle()
                        .set(STYLE_BACKGROUND, "#DBEAFE")
                        .set(STYLE_BORDER, "1px solid #93C5FD");
                styleNavChildren(link, VALUE_1D4ED8, "#1E40AF", "600");
            } else {
                link.getElement().setAttribute(STYLE_DATA_ACTIVE, "false");
                link.getElement().removeAttribute("aria-current");
                link.getStyle()
                        .set(STYLE_BACKGROUND, "transparent")
                        .set(STYLE_BORDER, "1px solid transparent");
                styleNavChildren(link, VALUE_4B5563, VALUE_1F2937, "500");
            }
        }
    }

    private void styleNavChildren(RouterLink link, String iconColor, String textColor, String weight) {
        link.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .map(HorizontalLayout.class::cast)
                .findFirst()
                .ifPresent(row -> row.getChildren().forEach(child -> {
                    if (child instanceof Icon icon) {
                        icon.getStyle().set(STYLE_COLOR, iconColor);
                    }
                    if (child instanceof Span span) {
                        span.getStyle().set(STYLE_COLOR, textColor).set(STYLE_FONT_WEIGHT, weight);
                    }
                }));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        highlightActive(event.getLocation().getPath());
        refreshHeaderAvatar(AuthSession.currentUser(userRepository).orElse(null));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isEmpty()) {
            event.forwardTo(LoginView.class);
        }
    }
}