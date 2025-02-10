package com.github.mvysny.vaadinsimplesecurity.externalauth.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.FailedLoginException;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A button which goes through the Google Identity authentication process.
 * The user clicks this button, goes through the Google login process, and ultimately logs in.
 * This button then verifies the Google security token and fires the {@link OnSignInEvent}.
 * <br/>
 * Register the {@link OnSignInEvent} listeners via {@link #addSignInListener(ComponentEventListener)}.
 */
@Tag("google-signin-button")
@JsModule("./src/google-signin-button.js")
@JavaScript(value = "https://accounts.google.com/gsi/client")
public class GoogleSignInButton extends Div {
    /**
     * Static field is OK since {@link NetHttpTransport} is thread safe.
     */
    @NotNull
    private static final NetHttpTransport HTTP_TRANSPORT;
    /**
     * Static field is OK since {@link JsonFactory} is thread safe.
     */
    @NotNull
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Holds the user information.
     *
     * @param email User's Google e-mail.
     * @param name  The full name of the user, e.g. "John Doe"
     */
    public record UserInfo(@NotNull String email,
                           @NotNull String name) implements Serializable {
    }

    /**
     * Fired when user attempts to sign in via Google Identity services. Either the
     * authentication went well (then the user is present in {@link #userInfo}),
     * or the authentication failed - then {@link #failure} is populated.
     * <br/>
     * If the authentication went well, that means that the token has been verified server-side
     * with Google, and therefore the information in {@link #getUserInfo()} can be trusted.
     */
    public static final class OnSignInEvent extends ComponentEvent<GoogleSignInButton> {
        @Nullable
        private final GoogleSignInButton.UserInfo userInfo;
        @Nullable
        private final Throwable failure;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         *                   side, <code>false</code> otherwise
         */
        public OnSignInEvent(@NotNull GoogleSignInButton source, boolean fromClient, @Nullable UserInfo userInfo, @Nullable Throwable failure) {
            super(source, fromClient);
            this.userInfo = userInfo;
            this.failure = failure;
        }

        public boolean isOk() {
            return userInfo != null;
        }

        public boolean isError() {
            return !isOk();
        }

        /**
         * If the Google Sign-In authentication succeeds, returns the information about the user.
         * @return the user info
         */
        public @Nullable UserInfo getUserInfo() {
            return userInfo;
        }

        /**
         * If the Google Sign-In authentication fails, this contains the failure cause.
         * @return the failure.
         */
        public @Nullable Throwable getFailure() {
            return failure;
        }
    }

    /**
     * The "Client ID" from Google OAuth 2.0 credential, looks like
     * <code>2398471023-asoifywerhewjkdlaj023842asdkl.apps.googleusercontent.com</code>
     */
    @NotNull
    private final String clientId;

    /**
     * Creates the Google Sign-in Button.
     * @param clientId The "Client ID" from Google OAuth 2.0 credential, looks like
     *      <code>2398471023-asoifywerhewjkdlaj023842asdkl.apps.googleusercontent.com</code>
     */
    public GoogleSignInButton(@NotNull String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        getElement().setProperty("client_id", clientId);
        setCancelOnTapOutside(true);
        setContext(Context.Signin);
        setItpSupport(false);
        setButtonType(Type.Standard);
        setButtonTheme(Theme.Outline);
        setButtonSize(Size.Large);
    }

    @ClientCallable
    private void onSignIn(@NotNull String idTokenString) {
        final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
                .setAudience(List.of(clientId))
                .build();
        try {
            final GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new FailedLoginException("Failed to verify credentials");
            }
            final String email = idToken.getPayload().getEmail();
            final String name = ((String) idToken.getPayload().get("name"));
            final UserInfo userInfo = new UserInfo(email, name);
            fireEvent(new OnSignInEvent(this, true, userInfo, null));
        } catch (Exception e) {
            fireEvent(new OnSignInEvent(this, true, null, e));
        }
    }

    /**
     * Listens for {@link OnSignInEvent}.
     * @param listener the listener
     * @return the registration, used to remove the listener.
     */
    @NotNull
    public Registration addSignInListener(@NotNull ComponentEventListener<OnSignInEvent> listener) {
        return addListener(OnSignInEvent.class, listener);
    }

    public boolean isCancelOnTapOutside() {
        return getElement().getProperty("cancel_on_tap_outside", true);
    }

    /**
     * This field sets whether or not to cancel the One Tap request if a user clicks outside the prompt.
     * The default value is true. You can disable it if you set the value to false.
     * @param cancelOnTapOutside
     */
    public void setCancelOnTapOutside(boolean cancelOnTapOutside) {
        getElement().setProperty("cancel_on_tap_outside", cancelOnTapOutside);
    }

    /**
     * This field changes the text of the title and messages in the One Tap prompt.
     */
    public enum Context {
        /**
         * "Sign in with Google"
         */
        Signin,
        /**
         * "Sign up Google"
         */
        Signup,
        /**
         * "Use with Google"
         */
        Use;
    }

    @NotNull
    public Context getContext() {
        final String context = getElement().getProperty("context", "signin");
        return Arrays.stream(Context.values()).filter(it -> it.name().equalsIgnoreCase(context)).findAny().orElse(Context.Signin);
    }

    /**
     * This field changes the text of the title and messages in the One Tap prompt.
     * @param context
     */
    public void setContext(@NotNull Context context) {
        getElement().setProperty("context", context.name().toLowerCase());
    }

    public boolean isItpSupport() {
        return getElement().getProperty("itp_support", false);
    }

    /**
     * This field determines if the <a href="https://developers.google.com/identity/gsi/web/guides/features#upgraded_ux_on_itp_browsers">upgraded One Tap UX</a> should be enabled on browsers that support Intelligent Tracking Prevention (ITP). The default value is false
     * @param itpSupport
     */
    public void setItpSupport(boolean itpSupport) {
        getElement().setProperty("itp_support", itpSupport);
    }

    @Nullable
    public String getLoginHint() {
        return getElement().getProperty("login_hint");
    }

    /**
     * If your application knows in advance which user should be signed-in, it can
     * provide a login hint to Google. When successful, account selection is skipped.
     * Accepted values are: an email address or an ID token
     * <a href="https://developers.google.com/identity/openid-connect/openid-connect#an-id-tokens-payload">sub</a> field value.
     * <br/>
     * For more information, see the <a href="https://developers.google.com/identity/protocols/oauth2/openid-connect#authenticationuriparameters">login_hint</a> field in the OpenID Connect documentation.
     * @param loginHint String, an email address or the value from an ID token sub field. For example 'elisa.beckett@gmail.com'
     */
    public void setLoginHint(@Nullable String loginHint) {
        getElement().setProperty("login_hint", loginHint);
    }

    @Nullable
    public String getHd() {
        return getElement().getProperty("hd");
    }

    /**
     * When a user has multiple accounts and should only sign-in with their Workspace
     * account use this to provide a domain name hint to Google. When successful,
     * user accounts displayed during account selection are limited to the provided domain.
     * A wildcard value: <code>*</code> offers only Workspace accounts to the user and
     * excludes consumer accounts (user@gmail.com) during account selection.
     * <br/>
     * For more information, see the <a href="https://developers.google.com/identity/protocols/oauth2/openid-connect#authenticationuriparameters">hd</a> field in the OpenID Connect documentation.
     * @param hd String. A fully qualified domain name or <code>*</code>
     */
    public void setHd(@Nullable String hd) {
        getElement().setProperty("hd", hd);
    }

    /**
     * The button type.
     */
    public enum Type {
        /**
         * Button with text or personalized information.
         */
        Standard,
        /**
         * An icon button without text.
         */
        Icon
    }

    @NotNull
    public Type getButtonType() {
        final String type = getElement().getProperty("button_type", "standard");
        return Arrays.stream(Type.values()).filter(it -> it.name().equalsIgnoreCase(type)).findAny().orElse(Type.Standard);
    }

    /**
     * The button type. The default value is standard.
     * @param type the button type.
     */
    public void setButtonType(@NotNull Type type) {
        getElement().setProperty("button_type", type.name().toLowerCase());
    }

    /**
     * The button theme.
     */
    public enum Theme {
        /**
         * A standard button theme
         */
        Outline,
        /**
         * A blue-filled button theme.
         */
        Filled_Blue,
        /**
         * A black-filled button theme.
         */
        Filled_Black;
    }

    @NotNull
    public Theme getButtonTheme() {
        final String theme = getElement().getProperty("button_theme", "outline");
        return Arrays.stream(Theme.values()).filter(it -> it.name().equalsIgnoreCase(theme)).findAny().orElse(Theme.Outline);
    }

    /**
     * The button theme. The default value is outline.
     * @param theme the button theme.
     */
    public void setButtonTheme(@NotNull Theme theme) {
        getElement().setProperty("button_theme", theme.name().toLowerCase());
    }

    /**
     * The button size
     */
    public enum Size {
        /**
         * A large button
         */
        Large,
        /**
         * A medium button
         */
        Medium,
        /**
         * A small button
         */
        Small
    }

    @NotNull
    public Size getButtonSize() {
        final String size = getElement().getProperty("button_size", "large");
        return Arrays.stream(Size.values()).filter(it -> it.name().equalsIgnoreCase(size)).findAny().orElse(Size.Large);
    }

    /**
     * The button size. The default value is large.
     * @param size the button size.
     */
    public void setButtonSize(@NotNull Size size) {
        getElement().setProperty("button_size", size.name().toLowerCase());
    }

    /**
     * The button shape.
     */
    public enum Shape {
        /**
         * The rectangular-shaped button. If used for the icon button type, then it's the same as square.
         */
        Recangular,
        /**
         * The pill-shaped button. If used for the icon button type, then it's the same as circle.
         */
        Pill,
        /**
         * The circle-shaped button. If used for the standard button type, then it's the same as pill.
         */
        Circle,
        /**
         * The square-shaped button. If used for the standard button type, then it's the same as rectangular.
         */
        Square
    }

    @NotNull
    public Shape getButtonShape() {
        var shape = getElement().getProperty("button_shape", "rectangular");
        return Arrays.stream(Shape.values()).filter(it -> it.name().equalsIgnoreCase(shape)).findAny().orElse(Shape.Recangular);
    }

    /**
     * The button shape. The default value is rectangular
     * @param shape the button shape
     */
    public void setButtonShape(@NotNull Shape shape) {
        getElement().setProperty("button_shape", shape.name().toLowerCase());
    }

    public enum ButtonText {
        Signin_With,
        Signup_With,
        Continue_With,
        Signin
    }

    @NotNull
    public ButtonText getButtonText() {
        final String text = getElement().getProperty("button_text", "signin_with");
        return Arrays.stream(ButtonText.values()).filter(it -> it.name().equalsIgnoreCase(text)).findAny().orElse(ButtonText.Signin_With);
    }

    /**
     * The button text. The default value is signin_with. There are no visual differences for the text of icon buttons that have different text attributes. The only exception is when the text is read for screen accessibility.
     * @param buttonText the button text
     */
    public void setButtonText(@NotNull ButtonText buttonText) {
        getElement().setProperty("button_text", buttonText.name().toLowerCase());
    }

    /**
     * The alignment of the Google logo. The default value is left. This attribute only applies to the standard button type.
     */
    public enum LogoAlignment {
        Left,
        Center
    }

    @NotNull
    public LogoAlignment getLogoAlignment() {
        final String align = getElement().getProperty("button_logo_alignment", "left");
        return Arrays.stream(LogoAlignment.values()).filter(it -> it.name().equalsIgnoreCase(align)).findAny().orElse(LogoAlignment.Left);
    }

    public void setLogoAlignment(@NotNull LogoAlignment logoAlignment) {
        getElement().setProperty("button_logo_alignment", logoAlignment.name().toLowerCase());
    }

    @Nullable
    public Integer getButtonMinWidth() {
        final String widthString = getElement().getProperty("button_width");
        return widthString == null ? null : Integer.parseInt(widthString);
    }

    /**
     * The minimum button width, in pixels. The maximum width is 400 pixels.
     * @param width the new minimum width, in pixels.
     */
    public void setButtonMinWidth(@Nullable Integer width) {
        getElement().setProperty("button_width", width == null ? null : width.toString());
    }
}
