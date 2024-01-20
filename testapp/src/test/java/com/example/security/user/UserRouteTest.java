package com.example.security.user;

import com.example.security.AbstractAppTester;
import com.example.security.security.LoginRoute;
import com.example.security.welcome.WelcomeRoute;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;

public class UserRouteTest extends AbstractAppTester {
    @Test
    public void loggedOutUserShouldNotBeAbleToSeeRoute() {
        UI.getCurrent().navigate(UserRoute.class);
        _assertOne(LoginRoute.class);
    }

    @Test
    public void userShouldSeeRoute() {
        login("user");
        UI.getCurrent().navigate(UserRoute.class);
        _assertOne(UserRoute.class);
    }

    @Test
    public void adminShouldSeeRoute() {
        login("admin");
        UI.getCurrent().navigate(UserRoute.class);
        _assertOne(UserRoute.class);
    }
}
