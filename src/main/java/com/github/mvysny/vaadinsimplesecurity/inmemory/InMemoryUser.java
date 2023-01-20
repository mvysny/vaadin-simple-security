package com.github.mvysny.vaadinsimplesecurity.inmemory;

import com.github.mvysny.vaadinsimplesecurity.HasPassword;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A simple demo example implementation of a user object, with username, password and roles.
 * Only used for demo/example purposes - your app will most likely use your own
 * <code>User</code> entity, probably loaded from the database.
 * <p></p>
 * Note the {@link HasPassword} interface which greatly helps with password management
 * and verification.
 */
public final class InMemoryUser implements Serializable, HasPassword {

    @NotNull
    private String hashedPassword = "";
    @NotNull
    private final String username;
    @NotNull
    private final Set<String> roles;

    public InMemoryUser(@NotNull String username, @NotNull String password, @NotNull Set<String> roles) {
        this.username = Objects.requireNonNull(username);
        this.roles = new HashSet<>(roles);
        setPassword(password);
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    @NotNull
    public Set<String> getRoles() {
        return new HashSet<>(roles);
    }

    @Override
    public @NotNull String getHashedPassword() {
        return hashedPassword;
    }

    @Override
    public void setHashedPassword(@NotNull String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    @Override
    public String toString() {
        return "InMemoryUser{'" + username + '\'' + ", roles=" + roles + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryUser that = (InMemoryUser) o;
        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
