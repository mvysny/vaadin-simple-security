package com.github.mvysny.vaadinsimplesecurity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.LoginException;
import java.util.Set;

/**
 * Directly logs in given user, no questions asked. No authentication is performed, such as
 * checking that the user exists or that the password matches. Use when your app only uses
 * external services to authenticate users, such as OAuth 2.0, OpenId, LDAP,
 * <a href="https://developers.google.com/identity/gsi/web/guides/overview">Google Identity</a>
 * or such.
 */
public final class DirectLoginService extends AbstractLoginService<SimpleUserWithRoles> {

    /**
     * Logs in given user, no questions asked. Never fails with {@link LoginException}.
     * Expects that the user has been authenticated by an external authentication system.
     */
    public void login(@NotNull String username, @Nullable Set<String> roles) {
        login(new SimpleUserWithRoles(username, roles));
    }

    @Override
    protected @NotNull SimpleUserWithRoles toUserWithRoles(@NotNull SimpleUserWithRoles user) {
        return user;
    }

    /**
     * Returns the service instance from Vaadin Session, creating it if it doesn't exist yet.
     * @return the service.
     */
    @NotNull
    public static DirectLoginService get() {
        return get(DirectLoginService.class, DirectLoginService::new);
    }
}
