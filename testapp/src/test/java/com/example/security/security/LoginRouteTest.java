package com.example.security.security;

import com.example.security.AbstractAppTester;
import com.example.security.welcome.WelcomeRoute;
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryLoginService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ.*;
import static com.github.mvysny.kaributesting.v10.LocatorKt._expectInternalServerError;
import static com.github.mvysny.kaributesting.v10.LoginFormKt._login;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginRouteTest extends AbstractAppTester {
    @Test
    public void unsuccessfulLogin() {
        _assertOne(LoginRoute.class); // check that initially the LoginView is displayed
        _login(_get(LoginForm.class), "invaliduser", "invaliduser");
        assertFalse(InMemoryLoginService.get().isLoggedIn());
        assertTrue(_get(LoginForm.class).isError());
    }

    @Test
    public void successfulLogin() {
        _assertOne(LoginRoute.class); // check that initially the LoginView is displayed
        _login(_get(LoginForm.class), "user", "user");
        assertTrue(InMemoryLoginService.get().isLoggedIn());
        // after successful login the WelcomeView should be displayed
        _assertNone(LoginRoute.class);
        _assertOne(WelcomeRoute.class);
    }

    @Test
    public void errorRouteNotHijackedByLoginView() {
        UI.getCurrent().addBeforeEnterListener(e -> e.rerouteToError(new RuntimeException("Simulated"), "Simulated"));
        UI.getCurrent().navigate(WelcomeRoute.class);
        _expectInternalServerError("Simulated");
    }
}
