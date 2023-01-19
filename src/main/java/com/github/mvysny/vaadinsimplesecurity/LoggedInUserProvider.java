package com.github.mvysny.vaadinsimplesecurity;

import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

/**
 * Resolves the currently logged-in user. Typically, you store the logged-in user
 * into a Vaadin session (or into a session-scoped <code>LoginService</code> class stored in a session);
 * then this provider simply loads the user from the {@link VaadinSession#getCurrent() current session}.
 * <p></p>
 * Methods on this interface can only be called from the Vaadin UI thread.
 * <p></p>
 * Called from {@link SimpleViewAccessChecker}.
 */
public interface LoggedInUserProvider extends Serializable {
    /**
     * Returns the currently logged-in user.
     * @return logged-in user if any, null if no user is currently logged in.
     * You can use {@link BasicUserPrincipal} for convenience.
     */
    @Nullable
    Principal getCurrentUser();

    /**
     * Returns the roles assigned to the currently logged-in user.
     * If there is no user logged in, or the user has no roles, the function
     * returns an empty set.
     * <p></p>
     * The roles are later on checked
     */
    @NotNull
    Set<String> getCurrentUserRoles();

    default boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Checks whether the currently logged-in user contains given role.
     * If there is no user currently logged in, the function will always
     * return false.
     */
    default boolean hasRole(@NotNull String role) {
        return getCurrentUserRoles().contains(role);
    }

    /**
     * Provider which always returns null user: the user is always logged out.
     */
    @NotNull
    LoggedInUserProvider LOGGED_OUT = new LoggedInUserProvider() {
        @Override
        public @Nullable Principal getCurrentUser() {
            return null;
        }

        @Override
        public @NotNull Set<String> getCurrentUserRoles() {
            return Collections.emptySet();
        }
    };
}
