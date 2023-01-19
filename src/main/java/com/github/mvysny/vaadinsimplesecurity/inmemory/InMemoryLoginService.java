package com.github.mvysny.vaadinsimplesecurity.inmemory;

import com.github.mvysny.vaadinsimplesecurity.SimpleViewAccessChecker;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Session-scoped service which holds currently logged-in user. Call {@link #login(String, String)}
 * to try to log in the user; call {@link #logout()} to logout user and redirect to the login page.
 * <p></p>
 * Don't forget to register {@link InMemoryLoggedInUserProvider};
 * also register {@link SimpleViewAccessChecker} as the before-navigation listener.
 */
public final class InMemoryLoginService implements Serializable {
    @Nullable
    private InMemoryUser currentUser = null;

    private InMemoryLoginService() {
    }

    /**
     * Returns the currently logged-in user, or null if there's no user logged in.
     * @return logged-in user, may be null.
     */
    @Nullable
    public InMemoryUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns true if the user is logged in (the {@link #getCurrentUser()} is not null), false if not.
     */
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Logs in user with given username and password. Fails with {@link LoginException}
     * on failure.
     */
    public void login(@NotNull String username, @NotNull String password) throws LoginException {
        final InMemoryUser user = InMemoryUserRegistry.getInstance().findByUsername(username);
        if (user == null) {
            throw new FailedLoginException("Invalid username or password");
        }
        if (!user.passwordMatches(password)) {
            throw new FailedLoginException("Invalid username or password");
        }
        login(user);
    }

    /**
     * Logs in given user.
     */
    private void login(@NotNull InMemoryUser user) {
        this.currentUser = user;

        // creates a new session after login, to prevent session fixation attack
        VaadinService.reinitializeSession(VaadinRequest.getCurrent());

        // navigate the user away from the LoginView and to the landing page.
        // all logged-in users must be able to see the landing page, otherwise they will
        // be redirected back to the LoginView.
        UI.getCurrent().navigate("");
    }

    /**
     * Logs out the user, clears the session and reloads the page.
     */
    public void logout() {
        VaadinSession.getCurrent().close();

        // The UI is recreated by the page reload, and since there is no user in the session (since it has been cleared),
        // the UI will show the LoginView.
        UI.getCurrent().getPage().reload();
    }

    @NotNull
    public Set<String> getCurrentUserRoles() {
        if (currentUser == null) {
            return Collections.emptySet();
        }
        return currentUser.getRoles();
    }

    public boolean isUserInRole(@NotNull String role) {
        return getCurrentUserRoles().contains(role);
    }

    /**
     * Returns the service instance from Vaadin Session, creating it if it doesn't exist yet.
     * @return the service.
     */
    @NotNull
    public static InMemoryLoginService get() {
        final VaadinSession session = Objects.requireNonNull(VaadinSession.getCurrent(), "Not called from Vaadin UI thread");
        InMemoryLoginService service = session.getAttribute(InMemoryLoginService.class);
        if (service == null) {
            service = new InMemoryLoginService();
            session.setAttribute(InMemoryLoginService.class, service);
        }
        return service;
    }
}
