package com.github.mvysny.vaadinsimplesecurity.externalauth.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

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
}
