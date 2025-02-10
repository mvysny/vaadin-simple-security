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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
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

    @NotNull
    private static final Logger log = LoggerFactory.getLogger(GoogleSignInButton.class);

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
        getElement().setProperty("clientId", clientId);
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
}
