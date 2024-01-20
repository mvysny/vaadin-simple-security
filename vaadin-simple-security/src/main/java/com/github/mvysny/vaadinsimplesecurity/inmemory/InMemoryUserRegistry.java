package com.github.mvysny.vaadinsimplesecurity.inmemory;

import com.github.mvysny.vaadinsimplesecurity.SimpleNavigationAccessControl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple in-memory singleton registry of known users. Call {@link #registerUser(InMemoryUser)}
 * to add more demo users.
 * <p></p>
 * Don't forget to register {@link SimpleNavigationAccessControl} as the before-navigation listener;
 * see {@link InMemoryLoginService} for a code example.
 * <p></p>
 * Thread-safe.
 */
public final class InMemoryUserRegistry {
    /**
     * Singleton instance of this registry.
     */
    @NotNull
    private static final InMemoryUserRegistry INSTANCE = new InMemoryUserRegistry();

    /**
     * Returns the singleton instance of this registry.
     * @return the singleton instance.
     */
    @NotNull
    public static InMemoryUserRegistry get() {
        return INSTANCE;
    }

    /**
     * All users registered via {@link #registerUser(InMemoryUser)}.
     */
    @NotNull
    private final CopyOnWriteArrayList<InMemoryUser> users = new CopyOnWriteArrayList<>();

    private InMemoryUserRegistry() {
    }

    /**
     * Adds a new user to this registry.
     * @param user the user to add.
     */
    public void registerUser(@NotNull InMemoryUser user) {
        users.add(Objects.requireNonNull(user));
    }

    /**
     * Removes all users.
     */
    public void clear() {
        users.clear();
    }

    /**
     * Finds the user by its username.
     * @param username the username, not null.
     * @return the user with given username or null if no such user has been
     * registered via {@link #registerUser(InMemoryUser)}.
     */
    @Nullable
    public InMemoryUser findByUsername(@NotNull String username) {
        Objects.requireNonNull(username);
        return users.stream()
                .filter(it -> it.getUsername().equals(username))
                .findAny()
                .orElse(null);
    }
}
