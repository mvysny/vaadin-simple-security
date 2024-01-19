package com.github.mvysny.vaadinsimplesecurity;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.function.Predicate;

/**
 * Checks that the current user has rights to access given route.
 * Obtains the user from given {@link #loggedInUserSupplier} rather than from
 * {@link HttpServletRequest#getUserPrincipal()} and {@link HttpServletRequest#isUserInRole(String)}.
 * <p></p>
 * Install this as a {@link BeforeEnterListener} to your UI via {@link UI#addBeforeEnterListener(BeforeEnterListener)}.
 * The best way to do that is to register your {@link VaadinServiceInitListener},
 * then install {@link UIInitListener} via {@link VaadinService#addUIInitListener(UIInitListener)},
 * then register this to your UI.
 */
public class SimpleNavigationAccessControl extends NavigationAccessControl {
    @NotNull
    private final SerializableSupplier<SimpleUserWithRoles> loggedInUserSupplier;

    /**
     * Creates the checker.
     * @param loggedInUserSupplier provides currently logged-in user.
     */
    public SimpleNavigationAccessControl(@NotNull SerializableSupplier<SimpleUserWithRoles> loggedInUserSupplier) {
        this.loggedInUserSupplier = loggedInUserSupplier;
    }

    @Override
    @Nullable
    protected Principal getPrincipal(@Nullable VaadinRequest request) {
        return loggedInUserSupplier.get();
    }

    @Override
    @NotNull
    protected Predicate<String> getRolesChecker(@Nullable VaadinRequest request) {
        return role -> {
            final SimpleUserWithRoles user = loggedInUserSupplier.get();
            return user != null && user.hasRole(role);
        };
    }

    /**
     * Creates the access checker which uses given service.
     * @param serviceSupplier looks up service from current session by calling the <code>Service.get()</code>.
     * @return the access checker.
     */
    @NotNull
    public static SimpleNavigationAccessControl usingService(@NotNull SerializableSupplier<? extends AbstractLoginService<?>> serviceSupplier) {
        return new SimpleNavigationAccessControl(() -> serviceSupplier.get().getCurrentPrincipal());
    }
}
