package com.example.security.security;

import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryLoginService;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginRoute extends VerticalLayout implements ComponentEventListener<AbstractLogin.LoginEvent> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(LoginRoute.class);

    @NotNull
    private final LoginForm login = new LoginForm();

    public LoginRoute() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        login.addLoginListener(this);
        final LoginI18n loginI18n = LoginI18n.createDefault();
        loginI18n.getForm().setTitle("Vaadin Simple Security Demo");
        loginI18n.setAdditionalInformation("Log in as user/user or admin/admin");
        login.setI18n(loginI18n);
        login.setForgotPasswordButtonVisible(false);
        add(login);
    }

    @Override
    public void onComponentEvent(@NotNull AbstractLogin.LoginEvent loginEvent) {
        try {
            InMemoryLoginService.get().login(loginEvent.getUsername(), loginEvent.getPassword());
        } catch (LoginException ex) {
            log.warn("Login failed", ex);
            login.setError(true);
        }
    }
}
