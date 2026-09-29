package com.github.mvysny.vaadinsimplesecurity.inmemory;

import com.github.mvysny.vaadinsimplesecurity.AbstractLoginService;
import com.github.mvysny.vaadinsimplesecurity.SimpleUserWithRoles;
import com.github.mvysny.vaadinsimplesecurity.SimpleNavigationAccessControl;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

/**
 * Session-scoped service which holds currently logged-in user. Call {@link #login(String, String)}
 * to try to log in the user; call {@link #logout()} to logout user and redirect to the login page.
 * <p>
 * Pass this service to the {@link SimpleNavigationAccessControl} when registering it as the before-navigation listener:
 * <pre>
 * var accessControl = SimpleNavigationAccessControl.usingService(InMemoryLoginService::get);
 * accessControl.setLoginView(LoginView.class);
 * ui.addBeforeEnterListener(accessControl);
 * </pre>
 */
public final class InMemoryLoginService extends AbstractLoginService<InMemoryUser> {
    private InMemoryLoginService() {
        // private, to prevent accidental instantiation by hand
    }

    /**
     * Logs in user with given username and password, looked up in {@link InMemoryUserRegistry}.
     * @param username the username.
     * @param password the plaintext password, as typed by the user.
     * @throws LoginException if there's no such user or the password doesn't match.
     */
    public void login(@NotNull String username, @NotNull String password) throws LoginException {
        final InMemoryUser user = InMemoryUserRegistry.get().findByUsername(username);
        if (user == null) {
            throw new FailedLoginException("Invalid username or password");
        }
        if (!user.passwordMatches(password)) {
            throw new FailedLoginException("Invalid username or password");
        }
        login(user);
    }

    /**
     * Logs in given user, no questions asked. Never fails with {@link LoginException}.
     * Expects that the user has been authenticated by an external authentication system.
     * @param user the user to log in.
     * @throws LoginException never thrown; kept for source compatibility with existing callers.
     */
    public void loginDirectly(@NotNull InMemoryUser user) throws LoginException {
        login(user);
    }

    @Override
    protected @NotNull SimpleUserWithRoles toUserWithRoles(@NotNull InMemoryUser user) {
        return new SimpleUserWithRoles(user.getUsername(), user.getRoles());
    }

    /**
     * Returns the service instance from Vaadin Session, creating it if it doesn't exist yet.
     * @return the service.
     */
    @NotNull
    public static InMemoryLoginService get() {
        return get(InMemoryLoginService.class, InMemoryLoginService::new);
    }
}
