package com.example.security;

import com.example.security.security.LoginRoute;
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryLoginService;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MainLayoutTest extends AbstractAppTester {
    @Test
    public void logOutButtonLogsTheUserOut() {
        login("user");
        _click(_get(Button.class, s -> s.withText("Log Out")));
        assertFalse(InMemoryLoginService.get().isLoggedIn());
        _assertOne(LoginRoute.class);
    }
}
