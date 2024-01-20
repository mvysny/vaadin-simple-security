package com.example.security.admin;

import com.example.security.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * The Administration view which only administrators may access. The administrator should be able to see/edit the list of users.
 */
@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Administration")
@RolesAllowed("ROLE_ADMIN")
public class AdminRoute extends VerticalLayout {
    public AdminRoute() {
        add(new H1("Administration pages"));
    }
}
