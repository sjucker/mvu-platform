package ch.mvurdorf.platform.ui;

import ch.mvurdorf.platform.security.AuthenticatedUser;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;

import static ch.mvurdorf.platform.utils.FormatUtil.formatInstant;

@Slf4j
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final BuildProperties buildProperties;

    private H1 viewTitle;

    private final AuthenticatedUser authenticatedUser;

    public MainLayout(AuthenticatedUser authenticatedUser, @Nullable BuildProperties buildProperties) {
        this.authenticatedUser = authenticatedUser;
        this.buildProperties = buildProperties;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(FontSize.LARGE, Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        var appName = new Span("MVU");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
        var header = new Header(appName);
        header.addClassNames(Padding.SMALL);

        var scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        var nav = new SideNav();

        var menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {
            if (entry.icon() != null) {
                nav.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
            } else {
                nav.addItem(new SideNavItem(entry.title(), entry.path()));
            }
        });

        return nav;
    }

    private Footer createFooter() {
        var footer = new Footer();
        var layout = new VerticalLayout();
        layout.setSpacing(false);

        var maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            var user = maybeUser.get();
            var logoutButton = new Button("Logout %s".formatted(user.getName()),
                                          _ -> authenticatedUser.logout());
            logoutButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            logoutButton.setIcon(VaadinIcon.EXIT_O.create());
            logoutButton.setWidthFull();
            layout.add(logoutButton);
        }

        if (buildProperties != null) {
            var version = new Span("Version: %s".formatted(buildProperties.getVersion()));
            version.addDoubleClickListener(_ -> log.error("testing error mail"));
            version.addClassNames(FontSize.XXSMALL);
            var time = new Span("Time: %s".formatted(formatInstant(buildProperties.getTime())));
            time.addClassNames(FontSize.XXSMALL);
            layout.add(version, time);
        }
        footer.add(layout);
        return footer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}
