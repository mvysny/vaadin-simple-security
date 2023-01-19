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
 * Obtains the user from {@link LoggedInUserProvider#CURRENT} rather than from
 * {@link HttpServletRequest#getUserPrincipal()} and {@link HttpServletRequest#isUserInRole(String)}.
 * <p></p>
 * Install this as a {@link BeforeEnterListener} to your UI via {@link UI#addBeforeEnterListener(BeforeEnterListener)}.
 * The best way to do that is to register your {@link VaadinServiceInitListener},
 * then install {@link UIInitListener} via {@link VaadinService#addUIInitListener(UIInitListener)},
 * then register this to your UI.
 * <p></p>
 * Don't forget to install a proper {@link LoggedInUserProvider#CURRENT logged-in user provider} for your project.
 */
public class SimpleViewAccessChecker extends ViewAccessChecker {
    @Override
    @Nullable
    protected Principal getPrincipal(@Nullable VaadinRequest request) {
        return LoggedInUserProvider.CURRENT.getCurrentUser();
    }

    @Override
    @NotNull
    protected Function<String, Boolean> getRolesChecker(@Nullable VaadinRequest request) {
        return role -> LoggedInUserProvider.CURRENT.getCurrentUserRoles().contains(role);
    }
}
