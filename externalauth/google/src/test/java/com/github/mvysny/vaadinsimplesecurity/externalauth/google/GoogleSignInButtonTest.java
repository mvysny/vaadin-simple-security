package com.github.mvysny.vaadinsimplesecurity.externalauth.google;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoogleSignInButtonTest {

    @NotNull
    private static final String CLIENT_ID = "2398471023-asoifywerhewjkdlaj023842asdkl.apps.googleusercontent.com";

    @BeforeEach public void fakeVaadin() {
        MockVaadin.setup();
    }
    @AfterEach
    public void tearDownVaadin() {
        MockVaadin.tearDown();
    }
    @Test
    public void smoke() {
        UI.getCurrent().add(new GoogleSignInButton(CLIENT_ID));
    }
    @Test
    public void smokeAPITest() {
        final GoogleSignInButton button = new GoogleSignInButton(CLIENT_ID);
        button.setItpSupport(false);
        assertEquals(GoogleSignInButton.Context.Signin, button.getContext());
        button.setContext(GoogleSignInButton.Context.Use);
        button.setCancelOnTapOutside(false);
        button.setLoginHint(null);
        button.setLoginHint("foo@bar.com");
        button.setHd(null);
        button.setHd("*");
        button.setHd("foo@bar.com");
        assertEquals(GoogleSignInButton.Type.Standard, button.getButtonType());
        button.setButtonType(GoogleSignInButton.Type.Icon);
        assertEquals(GoogleSignInButton.Theme.Outline, button.getButtonTheme());
        button.setButtonTheme(GoogleSignInButton.Theme.Filled_Black);
    }
}
