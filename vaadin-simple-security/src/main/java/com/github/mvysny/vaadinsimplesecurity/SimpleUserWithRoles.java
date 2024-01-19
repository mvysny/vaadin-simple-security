package com.github.mvysny.vaadinsimplesecurity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;
import java.util.Set;

/**
 * A basic user representation, with username and roles. Immutable, thread-safe.
 * <p></p>
 * Since {@link #username} uniquely identifies the user, this object {@link #equals(Object) equality}
 * is based on username alone - roles are ignored.
 */
public final class SimpleUserWithRoles implements Principal, Serializable {
    /**
     * The user name, uniquely identifies the user.
     */
    @NotNull
    private final String username;
    /**
     * The roles assigned to this user. See <a href="https://en.wikipedia.org/wiki/Role-based_access_control">Role-Based Access Control (RBAC)</a>
     * for more details.
     */
    @NotNull
    private final Set<String> roles;

    /**
     * Creates a user.
     * @param username the username, also returned as {@link Principal#getName()}.
     * @param roles optional set of roles for this user. null translates to empty set.
     */
    public SimpleUserWithRoles(@NotNull String username, @Nullable Set<String> roles) {
        this.username = username;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    /**
     * Creates a user with no roles.
     * @param username the username, also returned as {@link Principal#getName()}.
     */
    public SimpleUserWithRoles(@NotNull String username) {
        this(username, null);
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    /**
     * Returns the roles assigned to this user. See <a href="https://en.wikipedia.org/wiki/Role-based_access_control">Role-Based Access Control (RBAC)</a>
     * for more details.
     * @return user's roles. May be empty. Unmodifiable.
     */
    @NotNull
    public Set<String> getRoles() {
        return roles;
    }

    public boolean hasRole(@NotNull String role) {
        return getRoles().contains(role);
    }

    @Override
    @NotNull
    public String getName() {
        return username;
    }

    @Override
    public String toString() {
        return "BasicUserPrincipal{" + username + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleUserWithRoles that = (SimpleUserWithRoles) o;
        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
