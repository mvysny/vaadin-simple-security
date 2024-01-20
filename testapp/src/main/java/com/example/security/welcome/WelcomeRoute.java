package com.example.security.welcome;

import com.example.security.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

/**
 * The welcome view of the app, visible to all users. It is a vertical layout which lays out the child components vertically.
 * <p></p>
 * Note that the {@link Route} annotation defines the main layout with which this view is wrapped in. See {@link MainLayout} for details on how to
 * create an app-wide layout which hosts views.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Welcome")
@PermitAll
public class WelcomeRoute extends VerticalLayout {
    public WelcomeRoute() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        add(new H1("Welcome!"), new Span("This is a welcome view for all users; all logged-in users can see this content"));
    }
}
