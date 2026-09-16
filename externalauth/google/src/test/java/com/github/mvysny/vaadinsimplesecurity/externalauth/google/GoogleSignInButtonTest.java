package com.github.mvysny.vaadinsimplesecurity.externalauth.google;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

        assertFalse(button.isItpSupport());
        button.setItpSupport(true);
        assertTrue(button.isItpSupport());
        button.setItpSupport(false);
        assertFalse(button.isItpSupport());

        assertEquals(GoogleSignInButton.Context.Signin, button.getContext());
        button.setContext(GoogleSignInButton.Context.Use);
        assertEquals(GoogleSignInButton.Context.Use, button.getContext());

        assertTrue(button.isCancelOnTapOutside());
        button.setCancelOnTapOutside(false);
        assertFalse(button.isCancelOnTapOutside());

        assertNull(button.getLoginHint());
        button.setLoginHint("foo@bar.com");
        assertEquals("foo@bar.com", button.getLoginHint());
        button.setLoginHint(null);
        assertNull(button.getLoginHint());

        assertNull(button.getHd());
        button.setHd("*");
        assertEquals("*", button.getHd());
        button.setHd("foo@bar.com");
        assertEquals("foo@bar.com", button.getHd());
        button.setHd(null);
        assertNull(button.getHd());

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
        button.setButtonMinWidth(null);
        assertNull(button.getButtonMinWidth());

        button.addSignInListener(e -> {
            System.out.println(e);
        });
    }

    @Test
    public void signInWithBogusTokenFiresFailureEvent() throws Exception {
        final GoogleSignInButton button = new GoogleSignInButton(CLIENT_ID);
        UI.getCurrent().add(button);
        final List<GoogleSignInButton.OnSignInEvent> events = new ArrayList<>();
        button.addSignInListener(events::add);

        onSignIn(button, "this-is-not-a-google-id-token");

        assertEquals(1, events.size());
        final GoogleSignInButton.OnSignInEvent event = events.get(0);
        assertTrue(event.isError());
        assertFalse(event.isOk());
        assertNull(event.getUserInfo());
        // a malformed token fails to parse, before the verifier would call Google - the test stays offline
        assertInstanceOf(IllegalArgumentException.class, event.getFailure());
    }

    @Test
    public void successfulSignInEventCarriesUserInfo() {
        final GoogleSignInButton button = new GoogleSignInButton(CLIENT_ID);
        final GoogleSignInButton.UserInfo userInfo = new GoogleSignInButton.UserInfo("john@doe.com", "John Doe");
        final GoogleSignInButton.OnSignInEvent event = new GoogleSignInButton.OnSignInEvent(button, true, userInfo, null);

        assertTrue(event.isOk());
        assertFalse(event.isError());
        assertEquals(userInfo, event.getUserInfo());
        assertNull(event.getFailure());
    }

    /**
     * Calls the {@link com.vaadin.flow.component.ClientCallable} method the browser calls when Google
     * hands it an ID token. It is private, and Karibu has no helper for client-callables, hence reflection.
     */
    private static void onSignIn(@NotNull GoogleSignInButton button, @NotNull String idToken) throws Exception {
        final Method onSignIn = GoogleSignInButton.class.getDeclaredMethod("onSignIn", String.class);
        onSignIn.setAccessible(true);
        onSignIn.invoke(button, idToken);
    }
}
