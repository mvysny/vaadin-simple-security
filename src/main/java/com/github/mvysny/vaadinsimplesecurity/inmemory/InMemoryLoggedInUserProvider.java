package com.github.mvysny.vaadinsimplesecurity.inmemory;

import com.github.mvysny.vaadinsimplesecurity.BasicUserPrincipal;
import com.github.mvysny.vaadinsimplesecurity.LoggedInUserProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

/**
 * Provides currently logged-in users from the {@link InMemoryLoginService}.
 */
public class InMemoryLoggedInUserProvider implements LoggedInUserProvider {
    @Override
    public @Nullable Principal getCurrentUser() {
        final InMemoryUser currentUser = InMemoryLoginService.get().getCurrentUser();
        return currentUser == null ? null : new BasicUserPrincipal(currentUser.getUsername());
    }

    @Override
    public @NotNull Set<String> getCurrentUserRoles() {
        final InMemoryUser currentUser = InMemoryLoginService.get().getCurrentUser();
        return currentUser == null ? Collections.emptySet() : currentUser.getRoles();
    }
}
