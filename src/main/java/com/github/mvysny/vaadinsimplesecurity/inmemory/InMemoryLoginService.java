package com.github.mvysny.vaadinsimplesecurity.inmemory;

import com.github.mvysny.vaadinsimplesecurity.AbstractLoginService;
import com.github.mvysny.vaadinsimplesecurity.SimpleViewAccessChecker;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.util.Set;

/**
 * Session-scoped service which holds currently logged-in user. Call {@link #login(String, String)}
 * to try to log in the user; call {@link #logout()} to logout user and redirect to the login page.
 * <p></p>
 * Don't forget to register {@link InMemoryLoggedInUserProvider};
 * also register {@link SimpleViewAccessChecker} as the before-navigation listener.
 */
public final class InMemoryLoginService extends AbstractLoginService<InMemoryUser> {
    private InMemoryLoginService() {
    }

    @Override
    protected @NotNull Set<String> getRoles(@NotNull InMemoryUser user) {
        return user.getRoles();
    }

    /**
     * Logs in user with given username and password. Fails with {@link LoginException}
     * on failure.
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
     * Returns the service instance from Vaadin Session, creating it if it doesn't exist yet.
     * @return the service.
     */
    @NotNull
    public static InMemoryLoginService get() {
        return get(InMemoryLoginService.class, InMemoryLoginService::new);
    }
}
