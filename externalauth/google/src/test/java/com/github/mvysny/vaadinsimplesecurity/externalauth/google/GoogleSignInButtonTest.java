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

        // button styles
        assertEquals(GoogleSignInButton.Type.Standard, button.getButtonType());
        button.setButtonType(GoogleSignInButton.Type.Icon);
        assertEquals(GoogleSignInButton.Type.Icon, button.getButtonType());

        assertEquals(GoogleSignInButton.Theme.Outline, button.getButtonTheme());
        button.setButtonTheme(GoogleSignInButton.Theme.Filled_Black);
        assertEquals(GoogleSignInButton.Theme.Filled_Black, button.getButtonTheme());

        assertEquals(GoogleSignInButton.Size.Large, button.getButtonSize());
        button.setButtonSize(GoogleSignInButton.Size.Small);
        assertEquals(GoogleSignInButton.Size.Small, button.getButtonSize());

        assertEquals(GoogleSignInButton.Shape.Recangular, button.getButtonShape());
        button.setButtonShape(GoogleSignInButton.Shape.Circle);
        assertEquals(GoogleSignInButton.Shape.Circle, button.getButtonShape());

        assertEquals(GoogleSignInButton.ButtonText.Signin_With, button.getButtonText());
        button.setButtonText(GoogleSignInButton.ButtonText.Signup_With);
        assertEquals(GoogleSignInButton.ButtonText.Signup_With, button.getButtonText());

        assertEquals(GoogleSignInButton.LogoAlignment.Left, button.getLogoAlignment());
        button.setLogoAlignment(GoogleSignInButton.LogoAlignment.Center);
        assertEquals(GoogleSignInButton.LogoAlignment.Center, button.getLogoAlignment());

        assertNull(button.getButtonMinWidth());
        button.setButtonMinWidth(200);
        assertEquals(200, button.getButtonMinWidth());
    }
}
