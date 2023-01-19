package com.github.mvysny.vaadinsimplesecurity.inmemory;

import com.github.mvysny.vaadinsimplesecurity.SimpleViewAccessChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple in-memory singleton registry of known users. Call {@link #registerUser(InMemoryUser)}
 * to add more demo users.
 * <p></p>
 * Don't forget to register {@link InMemoryLoggedInUserProvider};
 * also register {@link SimpleViewAccessChecker} as the before-navigation listener.
 */
public final class InMemoryUserRegistry {
    @NotNull
    private static final InMemoryUserRegistry INSTANCE = new InMemoryUserRegistry();

    @NotNull
    public static InMemoryUserRegistry getInstance() {
        return INSTANCE;
    }

    @NotNull
    private final CopyOnWriteArrayList<InMemoryUser> users = new CopyOnWriteArrayList<>();

    private InMemoryUserRegistry() {
    }

    public void registerUser(@NotNull InMemoryUser user) {
        users.add(user);
    }

    @Nullable
    public InMemoryUser findByUsername(@NotNull String username) {
        return users.stream()
                .filter(it -> it.getUsername().equals(username))
                .findAny()
                .orElse(null);
    }
}