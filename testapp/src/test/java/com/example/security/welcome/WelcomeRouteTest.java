package com.example.security.welcome;

import com.example.security.AbstractAppTester;
import com.example.security.security.LoginRoute;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;

public class WelcomeRouteTest extends AbstractAppTester {
    @Test
    public void loggedOutUserShouldNotBeAbleToSeeWelcomeRoute() {
        UI.getCurrent().navigate(WelcomeRoute.class);
        _assertOne(LoginRoute.class);
    }

    @Test
    public void userShouldSeeWelcomeView() {
        login("user");
        UI.getCurrent().navigate(WelcomeRoute.class);
        _assertOne(WelcomeRoute.class);
    }

    @Test
    public void adminShouldSeeWelcomeView() {
        login("admin");
        UI.getCurrent().navigate(WelcomeRoute.class);
        _assertOne(WelcomeRoute.class);
    }
}
