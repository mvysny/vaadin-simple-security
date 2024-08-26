package com.example.security;

import com.example.security.security.LoginRoute;
import com.example.security.welcome.WelcomeRoute;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryLoginService;
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryUserRegistry;
import com.vaadin.flow.server.VaadinService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.security.auth.login.LoginException;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assertNone;
import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;

/**
 * Uses Karibu-Testing to test the app.
 */
public abstract class AbstractAppTester {
    @NotNull
    private static final Routes routes = new Routes().autoDiscoverViews("com.example.security");

    @BeforeAll
    public static void beforeAll() {
        new Bootstrap().contextInitialized(null);
    }

    @AfterAll
    public static void afterAll() {
        InMemoryUserRegistry.get().clear();
        new Bootstrap().contextDestroyed(null);
    }

    /**
     * Mocks the UI and logs in given user.
     */
    public static void login(@NotNull String username) {
        try {
            InMemoryLoginService.get().login(username, username);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
        // check that there is no LoginForm and everything is prepared
        _assertNone(LoginRoute.class);
        // in fact, by default the WelcomeView should be displayed
        _assertOne(WelcomeRoute.class);
    }

    @BeforeEach
    public void beforeEach() {
        MockVaadin.setup(routes);
    }

    @AfterEach
    public void afterEach() {
        MockVaadin.tearDown();
    }

    protected final boolean isProductionMode() {
        return VaadinService.getCurrent().getDeploymentConfiguration().isProductionMode();
    }
}
