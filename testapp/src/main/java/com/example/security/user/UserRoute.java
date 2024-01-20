package com.example.security.user;

import com.example.security.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "user", layout = MainLayout.class)
@PageTitle("User Content")
@RolesAllowed(value = { "ROLE_USER", "ROLE_ADMIN" })
public class UserRoute extends VerticalLayout {
    public UserRoute() {
        add(new H1("Important content for users"));
        add(new Span("A page intended for users only. Only users and admins can see this view."));
    }
}
