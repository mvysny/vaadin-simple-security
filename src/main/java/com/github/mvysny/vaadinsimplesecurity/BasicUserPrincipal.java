package com.github.mvysny.vaadinsimplesecurity;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

/**
 * A very simple [Principal] implementation.
 */
public final class BasicUserPrincipal implements Principal, Serializable {
    @NotNull
    private final String username;

    public BasicUserPrincipal(@NotNull String username) {
        this.username = username;
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
        BasicUserPrincipal that = (BasicUserPrincipal) o;
        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
