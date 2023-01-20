package com.github.mvysny.vaadinsimplesecurity.inmemory;

import com.github.mvysny.vaadinsimplesecurity.AbstractLoginService;
import com.github.mvysny.vaadinsimplesecurity.SimpleUserWithRoles;
import com.github.mvysny.vaadinsimplesecurity.SimpleViewAccessChecker;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

/**
 * Session-scoped service which holds currently logged-in user. Call {@link #login(String, String)}
 * to try to log in the user; call {@link #logout()} to logout user and redirect to the login page.
 * <p></p>
 * Pass this service to the {@link SimpleViewAccessChecker} when registering it as the before-navigation listener:
 * <pre>
 * val checker = SimpleViewAccessChecker.usingService(InMemoryLoginService::get);
 * checker.setLoginView(LoginView.class);
 * ui.addBeforeEnterListener(checker);
 * </pre>
 */
public final class InMemoryLoginService extends AbstractLoginService<InMemoryUser> {
    private InMemoryLoginService() {
        // private, to prevent accidental instantiation by hand
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
        SimpleViewAccessChecker.usingService(InMemoryLoginService::get);
        return get(InMemoryLoginService.class, InMemoryLoginService::new);
    }
}
