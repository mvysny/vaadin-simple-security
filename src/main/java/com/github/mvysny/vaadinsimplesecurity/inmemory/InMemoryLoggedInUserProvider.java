package com.github.mvysny.vaadinsimplesecurity.inmemory;

import com.github.mvysny.vaadinsimplesecurity.LoggedInUserProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

/**
 * Provides currently logged-in users from {@link InMemoryLoginService}.
 * Set this provider to {@link LoggedInUserProvider#CURRENT} to activate.
 */
public class InMemoryLoggedInUserProvider extends LoggedInUserProvider {
    @Override
    public @Nullable Principal getCurrentUser() {
        final InMemoryUser currentUser = InMemoryLoginService.get().getCurrentUser();
        return currentUser == null ? null : currentUser.toPrincipal();
    }

    @Override
    public @NotNull Set<String> getCurrentUserRoles() {
        final InMemoryUser currentUser = InMemoryLoginService.get().getCurrentUser();
        return currentUser == null ? Collections.emptySet() : currentUser.getRoles();
    }
}
