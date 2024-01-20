package com.example.security;

import com.example.security.admin.AdminRoute;
import com.example.security.user.UserRoute;
import com.example.security.welcome.WelcomeRoute;
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryLoginService;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;
import org.jetbrains.annotations.NotNull;

/**
 * The main layout. It uses the app-layout component which makes the app look like an Android Material app.
 */
public class MainLayout extends AppLayout implements RouterLayout {
    @NotNull
    private final Div contentPane = new Div();

    public MainLayout() {
        addToNavbar(new DrawerToggle(), new H3("Vaadin Simple Security Demo"));
        final SideNav sideNav = new SideNav();
        sideNav.addItem(new SideNavItem("Welcome", WelcomeRoute.class, VaadinIcon.NEWSPAPER.create()));
        sideNav.addItem(new SideNavItem("User Contents", UserRoute.class, VaadinIcon.LIST.create()));
        sideNav.addItem(new SideNavItem("Admin", AdminRoute.class, VaadinIcon.COG.create()));
        addToDrawer(sideNav, new Button("Log Out", VaadinIcon.SIGN_OUT.create(), e -> InMemoryLoginService.get().logout()));

        setContent(contentPane);
        contentPane.setSizeFull();
        contentPane.addClassName("app-content");
    }

    @Override
    public void showRouterLayoutContent(@NotNull HasElement content) {
        contentPane.getElement().appendChild(content.getElement());
    }
}
