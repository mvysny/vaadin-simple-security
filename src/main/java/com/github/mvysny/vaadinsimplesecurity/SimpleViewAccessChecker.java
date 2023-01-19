package com.github.mvysny.vaadinsimplesecurity;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.ViewAccessChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.function.Function;

/**
 * Checks that the current user has rights to access given route.
 * Obtains the user from given {@link LoggedInUserProvider} rather than from
 * {@link HttpServletRequest#getUserPrincipal()} and {@link HttpServletRequest#isUserInRole(String)}.
 * <p></p>
 * Install this as a {@link BeforeEnterListener} to your UI via {@link UI#addBeforeEnterListener(BeforeEnterListener)}.
 * The best way to do that is to register your {@link VaadinServiceInitListener},
 * then install {@link UIInitListener} via {@link VaadinService#addUIInitListener(UIInitListener)},
 * then register this to your UI.
 */
public class SimpleViewAccessChecker extends ViewAccessChecker {
    @NotNull
    private final LoggedInUserProvider loggedInUserProvider;

    /**
     * Creates the checker.
     * @param loggedInUserProvider obtain the user from this provider.
     */
    public SimpleViewAccessChecker(@NotNull LoggedInUserProvider loggedInUserProvider) {
        this.loggedInUserProvider = loggedInUserProvider;
    }

    @Override
    @Nullable
    protected Principal getPrincipal(@Nullable VaadinRequest request) {
        return loggedInUserProvider.getCurrentUser();
    }

    @Override
    @NotNull
    protected Function<String, Boolean> getRolesChecker(@Nullable VaadinRequest request) {
        return role -> loggedInUserProvider.getCurrentUserRoles().contains(role);
    }
}
